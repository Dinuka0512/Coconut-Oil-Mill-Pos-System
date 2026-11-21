using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Drawing;
using System.IO;
using System.Net.Sockets;
using System.Reflection;
using System.Security.Cryptography;
using System.ServiceProcess;
using System.Text;
using System.Windows.Forms;
using Microsoft.Win32;

/// <summary>
/// Step-by-step setup for Dilshan Oil Mill POS.
/// 1. Check disk space
/// 2. Check MySQL is installed/running
/// 3. Ask MySQL admin username/password and test connection
/// 4. Save DB config (provision app DB/user)
/// 5. Install and launch application
/// </summary>
public class SetupForm : Form
{
    private readonly Label step1Status = new Label();
    private readonly Label step2Status = new Label();
    private readonly Label step3Status = new Label();
    private readonly Label overallStatus = new Label();
    private readonly TextBox adminUser = new TextBox();
    private readonly TextBox adminPassword = new TextBox();
    private readonly Button recheckButton = new Button();
    private readonly Button nextButton = new Button();
    private readonly Button testButton = new Button();
    private readonly Button installButton = new Button();
    private readonly Button launchButton = new Button();
    private readonly Button uninstallButton = new Button();
    private readonly Button finishButton = new Button();
    private readonly TextBox log = new TextBox();

    private const string ConfigDirectoryName = "Dilshan Oil Mill";
    private const string AppDatabaseUser = "oil_app";
    private const string DatabaseName = "Oil";
    private const string AppDisplayName = "Dilshan oil mill";

    private bool diskOk;
    private bool mysqlOk;
    private bool connectionOk;
    private string installedUninstallString;
    private string installedLocation;

    public SetupForm()
    {
        Text = "Dilshan Oil Mill - Setup";
        Size = new Size(760, 680);
        StartPosition = FormStartPosition.CenterScreen;
        FormBorderStyle = FormBorderStyle.FixedDialog;
        MaximizeBox = false;
        Font = new Font("Segoe UI", 9);

        Controls.Add(new Label
        {
            Text = "Dilshan Oil Mill POS - Installation",
            Font = new Font("Segoe UI", 16, FontStyle.Bold),
            Location = new Point(24, 16),
            Size = new Size(700, 34)
        });

        int y = 60;
        y = AddStepRow("Step 1: Check disk space", step1Status, y);
        y = AddStepRow("Step 2: Check MySQL is installed/running", step2Status, y);

        recheckButton.Text = "Recheck";
        recheckButton.Location = new Point(466, y);
        recheckButton.Size = new Size(120, 34);
        recheckButton.Click += (s, e) => RunChecks();
        Controls.Add(recheckButton);

        nextButton.Text = "Next";
        nextButton.Location = new Point(596, y);
        nextButton.Size = new Size(120, 34);
        nextButton.Enabled = false;
        nextButton.Click += NextButton_Click;
        Controls.Add(nextButton);
        y += 46;

        GroupBox credentials = new GroupBox
        {
            Text = "Step 3: MySQL admin username & password",
            Location = new Point(24, y),
            Size = new Size(692, 110)
        };
        credentials.Controls.Add(new Label { Text = "Username:", Location = new Point(14, 30), Size = new Size(80, 22) });
        adminUser.Location = new Point(100, 27);
        adminUser.Size = new Size(200, 24);
        adminUser.Text = "root";
        credentials.Controls.Add(adminUser);

        credentials.Controls.Add(new Label { Text = "Password:", Location = new Point(330, 30), Size = new Size(80, 22) });
        adminPassword.Location = new Point(410, 27);
        adminPassword.Size = new Size(250, 24);
        adminPassword.UseSystemPasswordChar = true;
        credentials.Controls.Add(adminPassword);

        testButton.Text = "Test Connection";
        testButton.Location = new Point(410, 62);
        testButton.Size = new Size(120, 32);
        testButton.Enabled = false;
        testButton.Click += TestButton_Click;
        credentials.Controls.Add(testButton);

        step3Status.Location = new Point(540, 68);
        step3Status.Size = new Size(140, 24);
        step3Status.Font = new Font("Segoe UI", 9, FontStyle.Bold);
        credentials.Controls.Add(step3Status);
        Controls.Add(credentials);
        y += 122;

        installButton.Text = "Step 5: Install App";
        installButton.Location = new Point(496, y);
        installButton.Size = new Size(220, 38);
        installButton.Enabled = false;
        installButton.Click += InstallButton_Click;
        Controls.Add(installButton);

        launchButton.Text = "Launch";
        launchButton.Location = new Point(496, y);
        launchButton.Size = new Size(220, 38);
        launchButton.Visible = false;
        launchButton.Click += LaunchButton_Click;
        Controls.Add(launchButton);

        overallStatus.Location = new Point(24, y + 8);
        overallStatus.Size = new Size(460, 24);
        overallStatus.Font = new Font("Segoe UI", 9, FontStyle.Bold);
        Controls.Add(overallStatus);
        y += 50;

        uninstallButton.Text = "Uninstall";
        uninstallButton.Location = new Point(466, y);
        uninstallButton.Size = new Size(120, 34);
        uninstallButton.Visible = false;
        uninstallButton.Click += UninstallButton_Click;
        Controls.Add(uninstallButton);

        finishButton.Text = "Finish";
        finishButton.Location = new Point(596, y);
        finishButton.Size = new Size(120, 34);
        finishButton.Visible = false;
        finishButton.Click += (s, e) => Close();
        Controls.Add(finishButton);
        y += 44;

        log.Multiline = true;
        log.ReadOnly = true;
        log.ScrollBars = ScrollBars.Vertical;
        log.Location = new Point(24, y);
        log.Size = new Size(692, 140);
        log.Font = new Font("Consolas", 9);
        Controls.Add(log);

        Shown += (s, e) =>
        {
            if (CheckInstalled())
            {
                ShowUninstallMode();
            }
            else
            {
                RunChecks();
            }
        };
    }

    private int AddStepRow(string text, Label status, int y)
    {
        Controls.Add(new Label
        {
            Text = text,
            Location = new Point(24, y),
            Size = new Size(520, 24)
        });
        status.Location = new Point(560, y);
        status.Size = new Size(156, 24);
        status.Font = new Font("Segoe UI", 10, FontStyle.Bold);
        Controls.Add(status);
        return y + 34;
    }

    private void Log(string message)
    {
        log.AppendText(message + Environment.NewLine);
        log.SelectionStart = log.TextLength;
        log.ScrollToCaret();
        Application.DoEvents();
    }

    private static void SetStatus(Label label, bool ok, string message)
    {
        label.Text = (ok ? "✓ " : "X ") + message;
        label.ForeColor = ok ? Color.Green : Color.Red;
    }

    private void RunChecks()
    {
        log.Clear();
        diskOk = false;
        mysqlOk = false;
        connectionOk = false;
        step3Status.Text = "";
        testButton.Enabled = false;
        installButton.Enabled = false;
        nextButton.Enabled = false;
        overallStatus.Text = "";

        Log("=== Step 1: Disk space ===");
        try
        {
            DriveInfo drive = new DriveInfo(Path.GetPathRoot(
                Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData)));
            long freeMb = drive.AvailableFreeSpace / (1024 * 1024);
            diskOk = freeMb >= 600;
            SetStatus(step1Status, diskOk, diskOk ? "OK (" + freeMb + " MB free)" : "Not enough space");
            Log("Disk space: " + freeMb + " MB free on " + drive.Name);
            if (!diskOk) Log("FAIL: At least 600 MB free disk space is required.");
        }
        catch (Exception ex)
        {
            diskOk = false;
            SetStatus(step1Status, false, "Check failed");
            Log("FAIL: Could not check disk space - " + ex.Message);
        }

        Log("");
        Log("=== Step 2: MySQL check ===");
        try
        {
            string mysqlService = null;
            bool serviceRunning = false;
            foreach (ServiceController service in ServiceController.GetServices())
            {
                if (service.ServiceName.IndexOf("mysql", StringComparison.OrdinalIgnoreCase) >= 0)
                {
                    mysqlService = service.ServiceName;
                    serviceRunning = service.Status == ServiceControllerStatus.Running;
                    Log("MySQL service found: " + service.ServiceName + " (" + service.Status + ")");
                    break;
                }
            }

            if (mysqlService != null && !serviceRunning)
            {
                Log("MySQL service is stopped. Attempting to start...");
                try
                {
                    using (ServiceController controller = new ServiceController(mysqlService))
                    {
                        controller.Start();
                        controller.WaitForStatus(ServiceControllerStatus.Running, TimeSpan.FromSeconds(30));
                    }
                    Log("MySQL service started.");
                }
                catch (Exception ex)
                {
                    Log("WARN: Could not start MySQL service - " + ex.Message);
                }
            }

            bool portOpen = WaitForPort("127.0.0.1", 3306, 5);
            mysqlOk = portOpen;
            if (mysqlOk)
            {
                SetStatus(step2Status, true, "MySQL found");
                Log("MySQL port 3306: reachable.");
            }
            else
            {
                SetStatus(step2Status, false, "MySQL not found");
                Log("FAIL: MySQL is not installed/running on port 3306.");
                Log("Install MySQL Server first, then click Recheck/Next checks again.");
            }
        }
        catch (Exception ex)
        {
            mysqlOk = false;
            SetStatus(step2Status, false, "Check failed");
            Log("FAIL: Could not check MySQL - " + ex.Message);
        }

        Log("");
        if (diskOk && mysqlOk)
        {
            overallStatus.Text = "✓ Checks passed. Click Next.";
            overallStatus.ForeColor = Color.Green;
            nextButton.Enabled = true;
            Log("Checks passed. Click Next to continue.");
        }
        else
        {
            overallStatus.Text = "X Cannot continue setup. Fix failed checks.";
            overallStatus.ForeColor = Color.Red;
            nextButton.Enabled = false;
            Log("Cannot continue setup until failed checks are fixed.");
        }
    }

    private void NextButton_Click(object sender, EventArgs e)
    {
        RunChecks();
        if (!diskOk || !mysqlOk)
        {
            MessageBox.Show("Cannot continue. Fix failed checks first.", "Setup", MessageBoxButtons.OK, MessageBoxIcon.Warning);
            return;
        }
        testButton.Enabled = true;
        installButton.Enabled = false;
        connectionOk = false;
        step3Status.Text = "";
        Log("");
        Log("=== Step 3: Enter MySQL admin credentials and test connection ===");
        Log("Enter username/password, then click Test Connection.");
    }

    private void TestButton_Click(object sender, EventArgs e)
    {
        connectionOk = false;
        installButton.Enabled = false;
        step3Status.Text = "";

        string user = string.IsNullOrWhiteSpace(adminUser.Text) ? "root" : adminUser.Text.Trim();
        string password = adminPassword.Text;
        if (string.IsNullOrEmpty(password))
        {
            SetStatus(step3Status, false, "Password required");
            Log("FAIL: Enter MySQL admin password.");
            return;
        }

        try
        {
            Log("Testing connection for user '" + user + "'...");
            ValidateMySqlCredentials(user, password);
            connectionOk = true;
            SetStatus(step3Status, true, "Connection OK");
            overallStatus.Text = "✓ Connection OK. You can install.";
            overallStatus.ForeColor = Color.Green;
            installButton.Enabled = true;
            Log("SUCCESS: MySQL connection OK.");
            Log("Step 4 will save DB config. Step 5 will install and launch app.");
        }
        catch (Exception ex)
        {
            connectionOk = false;
            SetStatus(step3Status, false, "Connection failed");
            overallStatus.Text = "X Cannot continue. Connection failed.";
            overallStatus.ForeColor = Color.Red;
            installButton.Enabled = false;
            Log("FAIL: " + ex.Message);
        }
    }

    private void InstallButton_Click(object sender, EventArgs e)
    {
        if (!diskOk || !mysqlOk || !connectionOk)
        {
            MessageBox.Show("Complete all previous steps successfully before installing.", "Setup",
                MessageBoxButtons.OK, MessageBoxIcon.Warning);
            return;
        }

        installButton.Enabled = false;
        testButton.Enabled = false;
        nextButton.Enabled = false;
        recheckButton.Enabled = false;

        string user = string.IsNullOrWhiteSpace(adminUser.Text) ? "root" : adminUser.Text.Trim();
        string password = adminPassword.Text;

        try
        {
            Log("");
            Log("=== Step 4: Save DB config ===");
            Log("Creating/verifying Oil database and app user...");
            string appPassword = ProvisionAppUser(user, password);
            SaveDatabaseConfig(appPassword);
            Log("DB config saved to: " + Path.Combine(
                Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData),
                ConfigDirectoryName, "database.properties"));

            Log("");
            Log("=== Step 5: Install application ===");
            string installerPath = ExtractInstaller();
            Log("Launching application installer...");
            Process process = Process.Start(new ProcessStartInfo(installerPath)
            {
                UseShellExecute = false
            });
            if (process == null)
            {
                throw new Exception("Could not start application installer.");
            }

            System.Windows.Forms.Timer timer = new System.Windows.Forms.Timer();
            timer.Interval = 1000;
            timer.Tick += (s, args) =>
            {
                if (!process.HasExited) return;
                timer.Stop();
                timer.Dispose();
                try
                {
                    Log("Application installer exited with code " + process.ExitCode + ".");
                    CheckInstalled();
                    installButton.Visible = false;
                    launchButton.Visible = true;
                    launchButton.Enabled = true;
                    launchButton.BringToFront();
                    finishButton.Visible = true;
                    finishButton.Enabled = true;
                    finishButton.BringToFront();
                    overallStatus.Text = "✓ Setup completed. Click Launch to open app.";
                    overallStatus.ForeColor = Color.Green;
                    Log("Setup completed. Click Launch to open Dilshan Oil Mill.");
                }
                catch (Exception ex)
                {
                    overallStatus.Text = "X Installation failed.";
                    overallStatus.ForeColor = Color.Red;
                    Log("ERROR: " + ex.Message);
                    installButton.Visible = true;
                    installButton.Enabled = connectionOk;
                    testButton.Enabled = true;
                    nextButton.Enabled = diskOk && mysqlOk;
                    recheckButton.Enabled = true;
                }
            };
            timer.Start();
        }
        catch (Exception ex)
        {
            overallStatus.Text = "X Installation failed.";
            overallStatus.ForeColor = Color.Red;
            Log("ERROR: " + ex.Message);
            MessageBox.Show("Installation failed: " + ex.Message, "Setup",
                MessageBoxButtons.OK, MessageBoxIcon.Error);
            installButton.Enabled = connectionOk;
            testButton.Enabled = true;
            nextButton.Enabled = diskOk && mysqlOk;
            recheckButton.Enabled = true;
        }
    }

    private bool CheckInstalled()
    {
        installedUninstallString = null;
        installedLocation = null;
        try
        {
            using (RegistryKey uninstall = Registry.CurrentUser.OpenSubKey(
                @"Software\Microsoft\Windows\CurrentVersion\Uninstall"))
            {
                if (uninstall == null) return false;
                foreach (string subKeyName in uninstall.GetSubKeyNames())
                {
                    using (RegistryKey appKey = uninstall.OpenSubKey(subKeyName))
                    {
                        if (appKey == null) continue;
                        string displayName = appKey.GetValue("DisplayName") as string;
                        if (string.IsNullOrEmpty(displayName)) continue;
                        if (displayName.IndexOf(AppDisplayName, StringComparison.OrdinalIgnoreCase) < 0) continue;

                        installedUninstallString = appKey.GetValue("UninstallString") as string;
                        installedLocation = appKey.GetValue("InstallLocation") as string;
                        if (string.IsNullOrEmpty(installedLocation))
                        {
                            string local = Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData);
                            installedLocation = Path.Combine(local, AppDisplayName);
                        }
                        return !string.IsNullOrEmpty(installedUninstallString);
                    }
                }
            }
        }
        catch (Exception ex)
        {
            Log("WARN: Could not check installed app - " + ex.Message);
        }
        return false;
    }

    private void ShowUninstallMode()
    {
        diskOk = mysqlOk = connectionOk = false;
        recheckButton.Visible = false;
        nextButton.Visible = false;
        testButton.Visible = false;
        installButton.Visible = false;
        launchButton.Visible = false;
        adminUser.Enabled = false;
        adminPassword.Enabled = false;
        uninstallButton.Visible = true;
        finishButton.Visible = true;
        finishButton.Enabled = true;

        log.Clear();
        Log("Dilshan Oil Mill is already installed.");
        Log("Install location: " + (installedLocation ?? "unknown"));
        overallStatus.Text = "Already installed.";
        overallStatus.ForeColor = Color.DarkOrange;

        DialogResult result = MessageBox.Show(
            "Dilshan Oil Mill is already installed.\n\nDo you want to uninstall it?",
            "Dilshan Oil Mill Setup",
            MessageBoxButtons.YesNo,
            MessageBoxIcon.Question);

        if (result == DialogResult.Yes)
        {
            UninstallButton_Click(this, EventArgs.Empty);
        }
        else
        {
            Log("Uninstall cancelled. Click Finish to close setup.");
            overallStatus.Text = "Uninstall cancelled. Click Finish to close.";
        }
    }

    private void UninstallButton_Click(object sender, EventArgs e)
    {
        if (string.IsNullOrEmpty(installedUninstallString))
        {
            MessageBox.Show("Uninstall command not found.", "Setup", MessageBoxButtons.OK, MessageBoxIcon.Error);
            return;
        }

        uninstallButton.Enabled = false;
        finishButton.Enabled = false;
        try
        {
            Log("Uninstalling existing Dilshan Oil Mill...");
            string cmd = installedUninstallString.Trim();
            string file;
            string args;
            if (cmd.StartsWith("\""))
            {
                int end = cmd.IndexOf('"', 1);
                file = cmd.Substring(1, Math.Max(0, end - 1));
                args = end > 0 && end + 1 < cmd.Length ? cmd.Substring(end + 1).Trim() : "";
            }
            else
            {
                int space = cmd.IndexOf(' ');
                file = space > 0 ? cmd.Substring(0, space) : cmd;
                args = space > 0 ? cmd.Substring(space + 1).Trim() : "";
            }

            Process process = Process.Start(new ProcessStartInfo(file, args)
            {
                UseShellExecute = false
            });
            if (process == null)
            {
                throw new Exception("Could not start uninstaller.");
            }

            System.Windows.Forms.Timer timer = new System.Windows.Forms.Timer();
            timer.Interval = 1000;
            timer.Tick += (s, argsTick) =>
            {
                if (!process.HasExited) return;
                timer.Stop();
                timer.Dispose();
                Log("Uninstaller exited with code " + process.ExitCode + ".");
                overallStatus.Text = "✓ Uninstall finished. Click Finish to close.";
                overallStatus.ForeColor = Color.Green;
                finishButton.Enabled = true;
                Log("Uninstall completed. Click Finish to close setup.");
            };
            timer.Start();
        }
        catch (Exception ex)
        {
            overallStatus.Text = "X Uninstall failed.";
            overallStatus.ForeColor = Color.Red;
            Log("ERROR: " + ex.Message);
            MessageBox.Show("Uninstall failed: " + ex.Message, "Setup", MessageBoxButtons.OK, MessageBoxIcon.Error);
            uninstallButton.Enabled = true;
            finishButton.Enabled = true;
        }
    }

    private void LaunchButton_Click(object sender, EventArgs e)
    {
        try
        {
            string exePath = FindInstalledExe();
            if (string.IsNullOrEmpty(exePath) || !File.Exists(exePath))
            {
                throw new Exception("Could not find installed Dilshan Oil Mill executable.");
            }
            Process.Start(new ProcessStartInfo(exePath) { UseShellExecute = true });
            Close();
        }
        catch (Exception ex)
        {
            MessageBox.Show("Could not launch app: " + ex.Message, "Setup", MessageBoxButtons.OK, MessageBoxIcon.Error);
        }
    }

    private string FindInstalledExe()
    {
        if (!string.IsNullOrEmpty(installedLocation))
        {
            string candidate = Path.Combine(installedLocation, AppDisplayName + ".exe");
            if (File.Exists(candidate)) return candidate;
            candidate = Path.Combine(installedLocation, "Dilshan Oil Mill.exe");
            if (File.Exists(candidate)) return candidate;
        }

        try
        {
            using (RegistryKey uninstall = Registry.CurrentUser.OpenSubKey(
                @"Software\Microsoft\Windows\CurrentVersion\Uninstall"))
            {
                if (uninstall != null)
                {
                    foreach (string subKeyName in uninstall.GetSubKeyNames())
                    {
                        using (RegistryKey appKey = uninstall.OpenSubKey(subKeyName))
                        {
                            if (appKey == null) continue;
                            string displayName = appKey.GetValue("DisplayName") as string;
                            if (string.IsNullOrEmpty(displayName)) continue;
                            if (displayName.IndexOf(AppDisplayName, StringComparison.OrdinalIgnoreCase) < 0) continue;
                            string loc = appKey.GetValue("InstallLocation") as string;
                            if (!string.IsNullOrEmpty(loc))
                            {
                                string exe = Path.Combine(loc, AppDisplayName + ".exe");
                                if (File.Exists(exe)) return exe;
                            }
                        }
                    }
                }
            }
        }
        catch { }

        string local = Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData);
        string fallback = Path.Combine(local, AppDisplayName, AppDisplayName + ".exe");
        return fallback;
    }

    private static void ValidateMySqlCredentials(string user, string password)
    {
        string mysqlExe = FindMySqlExe();
        if (mysqlExe == null)
        {
            throw new Exception("Could not find mysql.exe. Make sure MySQL client/server is installed.");
        }

        string stdout, stderr;
        Dictionary<string, string> env = new Dictionary<string, string> { { "MYSQL_PWD", password } };
        int exit = RunProcess(mysqlExe,
            "--host=127.0.0.1 --port=3306 --user=" + Quote(user) +
            " --batch --skip-column-names --execute=\"SELECT 1\"",
            null, 60, out stdout, out stderr, env);

        if (exit != 0)
        {
            throw new Exception("MySQL credentials rejected: " + FirstNonEmpty(stderr, stdout, "access denied"));
        }
    }

    private static string ProvisionAppUser(string adminUser, string adminPassword)
    {
        string mysqlExe = FindMySqlExe();
        if (mysqlExe == null)
        {
            throw new Exception("Could not find mysql.exe.");
        }

        string password = GeneratePassword();
        string sql =
            "CREATE DATABASE IF NOT EXISTS `" + DatabaseName + "` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;" +
            "CREATE USER IF NOT EXISTS '" + AppDatabaseUser + "'@'localhost' IDENTIFIED BY '" + SqlEscape(password) + "';" +
            "ALTER USER '" + AppDatabaseUser + "'@'localhost' IDENTIFIED BY '" + SqlEscape(password) + "';" +
            "CREATE USER IF NOT EXISTS '" + AppDatabaseUser + "'@'127.0.0.1' IDENTIFIED BY '" + SqlEscape(password) + "';" +
            "ALTER USER '" + AppDatabaseUser + "'@'127.0.0.1' IDENTIFIED BY '" + SqlEscape(password) + "';" +
            "GRANT ALL PRIVILEGES ON `" + DatabaseName + "`.* TO '" + AppDatabaseUser + "'@'localhost';" +
            "GRANT ALL PRIVILEGES ON `" + DatabaseName + "`.* TO '" + AppDatabaseUser + "'@'127.0.0.1';" +
            "FLUSH PRIVILEGES;";

        string args = "--host=127.0.0.1 --port=3306 --user=" + Quote(adminUser) +
                      " --batch --execute=" + Quote(sql);
        string stdout, stderr;
        Dictionary<string, string> env = new Dictionary<string, string> { { "MYSQL_PWD", adminPassword } };
        int exit = RunProcess(mysqlExe, args, null, 120, out stdout, out stderr, env);
        if (exit != 0)
        {
            throw new Exception("Could not create DB/app user: " + FirstNonEmpty(stderr, stdout, "unknown database error"));
        }
        return password;
    }

    private static void SaveDatabaseConfig(string appPassword)
    {
        string directory = Path.Combine(
            Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData),
            ConfigDirectoryName);
        Directory.CreateDirectory(directory);
        string path = Path.Combine(directory, "database.properties");

        StringBuilder content = new StringBuilder();
        content.AppendLine("# Generated by Dilshan Oil Mill Setup. Do not edit manually.");
        content.AppendLine("hibernate.connection.driver_class=com.mysql.cj.jdbc.Driver");
        content.AppendLine("hibernate.connection.url=jdbc:mysql://localhost:3306/" + DatabaseName +
                           "?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true");
        content.AppendLine("hibernate.connection.username=" + AppDatabaseUser);
        content.AppendLine("hibernate.connection.password=" + appPassword);
        content.AppendLine("hibernate.dialect=org.hibernate.dialect.MySQLDialect");
        content.AppendLine("hibernate.hbm2ddl.auto=validate");
        content.AppendLine("hibernate.show_sql=false");
        File.WriteAllText(path, content.ToString(), Encoding.UTF8);
    }

    private static string GeneratePassword()
    {
        byte[] bytes = new byte[24];
        using (RandomNumberGenerator rng = RandomNumberGenerator.Create())
        {
            rng.GetBytes(bytes);
        }
        return Convert.ToBase64String(bytes).Replace("+", "A").Replace("/", "B").Replace("=", "C");
    }

    private static string SqlEscape(string value)
    {
        return value.Replace("\\", "\\\\").Replace("'", "\\'");
    }

    private static string Quote(string value)
    {
        return "\"" + value.Replace("\"", "\\\"") + "\"";
    }

    private static string FirstNonEmpty(params string[] values)
    {
        foreach (string value in values)
        {
            if (!string.IsNullOrWhiteSpace(value))
            {
                return value.Trim();
            }
        }
        return string.Empty;
    }

    private static int RunProcess(string file, string arguments, string input,
        int timeoutSeconds, out string stdout, out string stderr,
        Dictionary<string, string> envVars = null)
    {
        ProcessStartInfo psi = new ProcessStartInfo(file)
        {
            Arguments = arguments,
            UseShellExecute = false,
            CreateNoWindow = true,
            RedirectStandardOutput = true,
            RedirectStandardError = true
        };
        if (input != null)
        {
            psi.RedirectStandardInput = true;
        }
        if (envVars != null)
        {
            foreach (KeyValuePair<string, string> entry in envVars)
            {
                psi.EnvironmentVariables[entry.Key] = entry.Value;
            }
        }

        StringBuilder outBuffer = new StringBuilder();
        StringBuilder errBuffer = new StringBuilder();
        using (Process process = new Process { StartInfo = psi })
        {
            process.OutputDataReceived += (s, e) => { if (e.Data != null) outBuffer.AppendLine(e.Data); };
            process.ErrorDataReceived += (s, e) => { if (e.Data != null) errBuffer.AppendLine(e.Data); };
            process.Start();
            process.BeginOutputReadLine();
            process.BeginErrorReadLine();

            if (input != null)
            {
                process.StandardInput.Write(input);
                process.StandardInput.Close();
            }

            if (!process.WaitForExit(timeoutSeconds * 1000))
            {
                try { process.Kill(); } catch { }
                stdout = outBuffer.ToString();
                stderr = errBuffer.ToString();
                throw new Exception("Timed out waiting for " + Path.GetFileName(file));
            }
            process.WaitForExit();
            stdout = outBuffer.ToString();
            stderr = errBuffer.ToString();
            return process.ExitCode;
        }
    }

    private static string FindMySqlExe()
    {
        try
        {
            foreach (ServiceController service in ServiceController.GetServices())
            {
                if (service.ServiceName.IndexOf("mysql", StringComparison.OrdinalIgnoreCase) < 0) continue;
                using (RegistryKey key = Registry.LocalMachine.OpenSubKey(
                    "SYSTEM\\CurrentControlSet\\Services\\" + service.ServiceName))
                {
                    string imagePath = key == null ? null : key.GetValue("ImagePath") as string;
                    if (string.IsNullOrEmpty(imagePath)) continue;
                    string mysqld = imagePath.Trim();
                    if (mysqld.StartsWith("\""))
                    {
                        int end = mysqld.IndexOf('"', 1);
                        if (end > 0) mysqld = mysqld.Substring(1, end - 1);
                    }
                    else
                    {
                        int space = mysqld.IndexOf(' ');
                        if (space > 0) mysqld = mysqld.Substring(0, space);
                    }
                    string mysql = Path.Combine(Path.GetDirectoryName(mysqld), "mysql.exe");
                    if (File.Exists(mysql)) return mysql;
                }
            }
        }
        catch { }

        string[] roots =
        {
            Environment.GetFolderPath(Environment.SpecialFolder.ProgramFiles),
            Environment.GetFolderPath(Environment.SpecialFolder.ProgramFilesX86)
        };
        foreach (string root in roots)
        {
            if (string.IsNullOrEmpty(root)) continue;
            try
            {
                string mysqlRoot = Path.Combine(root, "MySQL");
                if (!Directory.Exists(mysqlRoot)) continue;
                string[] found = Directory.GetFiles(mysqlRoot, "mysql.exe", SearchOption.AllDirectories);
                if (found.Length > 0) return found[0];
            }
            catch { }
        }
        return null;
    }

    private static bool IsPortOpen(string host, int port)
    {
        try
        {
            using (TcpClient client = new TcpClient())
            {
                IAsyncResult result = client.BeginConnect(host, port, null, null);
                bool connected = result.AsyncWaitHandle.WaitOne(TimeSpan.FromSeconds(3));
                if (connected) client.EndConnect(result);
                return connected;
            }
        }
        catch
        {
            return false;
        }
    }

    private static bool WaitForPort(string host, int port, int seconds)
    {
        DateTime deadline = DateTime.Now.AddSeconds(seconds);
        while (DateTime.Now < deadline)
        {
            if (IsPortOpen(host, port)) return true;
            Application.DoEvents();
            System.Threading.Thread.Sleep(500);
        }
        return false;
    }

    private static string ExtractInstaller()
    {
        return ExtractResource("appinstaller", "Dilshan oil mill-1.0.exe");
    }

    private static string ExtractResource(string resourceName, string fileName)
    {
        string directory = Path.Combine(Path.GetTempPath(),
            "DilshanOilMillSetup_" + Guid.NewGuid().ToString("N"));
        Directory.CreateDirectory(directory);
        string path = Path.Combine(directory, fileName);
        using (Stream source = Assembly.GetExecutingAssembly().GetManifestResourceStream(resourceName))
        {
            if (source == null)
            {
                throw new Exception("The embedded resource '" + resourceName + "' is missing.");
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
