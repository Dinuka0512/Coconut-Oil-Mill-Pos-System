using System;
using System.Diagnostics;
using System.Drawing;
using System.IO;
using System.Net.Sockets;
using System.Reflection;
using System.ServiceProcess;
using System.Threading;
using System.Windows.Forms;

/// <summary>
/// Prerequisite checker / launcher for the Dilshan Oil Mill POS installer.
/// Java 21 and the Bebas Neue font are bundled inside the application installer,
/// so only MySQL has to be present on the target machine.
/// </summary>
public class SetupForm : Form
{
    private readonly TextBox log = new TextBox();
    private readonly Button installButton = new Button();
    private bool prerequisitesOk;

    public SetupForm()
    {
        Text = "Dilshan Oil Mill - Setup";
        Size = new Size(760, 540);
        StartPosition = FormStartPosition.CenterScreen;
        FormBorderStyle = FormBorderStyle.FixedDialog;
        MaximizeBox = false;

        Label title = new Label
        {
            Text = "Dilshan Oil Mill POS - Installation",
            Font = new Font("Segoe UI", 16, FontStyle.Bold),
            Location = new Point(24, 20),
            Size = new Size(700, 32)
        };
        Controls.Add(title);

        Label hint = new Label
        {
            Text = "Checking the requirements for this machine. MySQL Server 8 must already be installed and running.",
            Location = new Point(24, 58),
            Size = new Size(700, 24)
        };
        Controls.Add(hint);

        log.Multiline = true;
        log.ReadOnly = true;
        log.ScrollBars = ScrollBars.Vertical;
        log.Location = new Point(24, 92);
        log.Size = new Size(692, 340);
        log.Font = new Font("Consolas", 10);
        Controls.Add(log);

        installButton.Text = "Install Application";
        installButton.Location = new Point(560, 452);
        installButton.Size = new Size(156, 40);
        installButton.Enabled = false;
        installButton.Click += InstallButton_Click;
        Controls.Add(installButton);

        Shown += (s, e) => RunChecks();
    }

    private void Log(string message)
    {
        log.AppendText(message + Environment.NewLine);
        log.SelectionStart = log.TextLength;
        log.ScrollToCaret();
        Application.DoEvents();
    }

    private void RunChecks()
    {
        log.Clear();
        prerequisitesOk = true;

        Log("=== Requirement check ===");
        Log("Windows: " + Environment.OSVersion.VersionString +
            (Environment.Is64BitOperatingSystem ? " (64-bit)" : " (32-bit)"));
        if (!Environment.Is64BitOperatingSystem)
        {
            Log("FAIL: A 64-bit Windows machine is required.");
            prerequisitesOk = false;
        }

        try
        {
            DriveInfo drive = new DriveInfo(Path.GetPathRoot(Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData)));
            long freeMb = drive.AvailableFreeSpace / (1024 * 1024);
            Log("Disk space: " + freeMb + " MB free on " + drive.Name);
            if (freeMb < 600)
            {
                Log("FAIL: At least 600 MB of free disk space is required.");
                prerequisitesOk = false;
            }
        }
        catch (Exception ex)
        {
            Log("WARN: Could not check disk space - " + ex.Message);
        }

        // MySQL is the only external requirement. Java and the font ship inside the app.
        bool mysqlService = false;
        bool serviceRunning = false;
        try
        {
            foreach (ServiceController service in ServiceController.GetServices())
            {
                if (service.ServiceName.IndexOf("mysql", StringComparison.OrdinalIgnoreCase) >= 0)
                {
                    mysqlService = true;
                    serviceRunning = service.Status == ServiceControllerStatus.Running;
                    Log("MySQL service: " + service.ServiceName + " (" + service.Status + ")");
                    break;
                }
            }
        }
        catch (Exception ex)
        {
            Log("WARN: Could not query Windows services - " + ex.Message);
        }

        if (!mysqlService)
        {
            Log("MySQL service: not found.");
        }

        bool portOpen = IsPortOpen("127.0.0.1", 3306);
        Log("MySQL port 3306: " + (portOpen ? "reachable" : "not reachable"));

        if (!portOpen)
        {
            Log("FAIL: MySQL Server is not running or is not listening on port 3306.");
            if (!serviceRunning)
            {
                Log("       Start the MySQL service (for example: MySQL80) and run this setup again.");
            }
            prerequisitesOk = false;
        }
        else
        {
            Log("Database access: the app uses root / root and will create the Oil database and tables on first launch.");
        }

        Log("Java runtime: bundled Java 21 runtime included in the application installer - no system Java needed.");
        Log("Bebas Neue font: bundled with the application - no system font installation needed.");

        string systemFont = Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.Fonts), "BEBASNE__.TTF");
        Log("System Bebas Neue font: " + (File.Exists(systemFont) ? "installed" : "not installed (not required)"));

        Log(string.Empty);
        if (prerequisitesOk)
        {
            Log("All checks passed. Click 'Install Application' to continue.");
        }
        else
        {
            Log("Installation is blocked until the failed checks above are fixed.");
        }
        installButton.Enabled = prerequisitesOk;
    }

    private static bool IsPortOpen(string host, int port)
    {
        try
        {
            using (TcpClient client = new TcpClient())
            {
                IAsyncResult result = client.BeginConnect(host, port, null, null);
                bool connected = result.AsyncWaitHandle.WaitOne(TimeSpan.FromSeconds(3));
                if (connected)
                {
                    client.EndConnect(result);
                }
                return connected;
            }
        }
        catch
        {
            return false;
        }
    }

    private void InstallButton_Click(object sender, EventArgs e)
    {
        installButton.Enabled = false;
        try
        {
            Log(string.Empty);
            Log("=== Installation ===");
            string installerPath = ExtractInstaller();
            Log("Launching application installer...");
            Process process = Process.Start(new ProcessStartInfo(installerPath)
            {
                UseShellExecute = false
            });
            if (process != null)
            {
                process.WaitForExit();
                Log("Application installer exited with code " + process.ExitCode + ".");
            }
            MessageBox.Show("The application installer has finished.", "Dilshan Oil Mill Setup",
                MessageBoxButtons.OK, MessageBoxIcon.Information);
        }
        catch (Exception ex)
        {
            Log("ERROR: " + ex.Message);
            MessageBox.Show("Installation failed: " + ex.Message, "Dilshan Oil Mill Setup",
                MessageBoxButtons.OK, MessageBoxIcon.Error);
        }
        finally
        {
            installButton.Enabled = true;
        }
    }

    private static string ExtractInstaller()
    {
        string directory = Path.Combine(Path.GetTempPath(),
            "DilshanOilMillSetup_" + Guid.NewGuid().ToString("N"));
        Directory.CreateDirectory(directory);
        string path = Path.Combine(directory, "Dilshan oil mill-1.0.exe");
        using (Stream source = Assembly.GetExecutingAssembly().GetManifestResourceStream("appinstaller"))
        {
            if (source == null)
            {
                throw new Exception("The embedded application installer is missing.");
            }
            using (FileStream target = File.Create(path))
            {
                source.CopyTo(target);
            }
        }
        return path;
    }

    [STAThread]
    public static void Main()
    {
        Application.EnableVisualStyles();
        Application.SetCompatibleTextRenderingDefault(false);
        Application.Run(new SetupForm());
    }
}
