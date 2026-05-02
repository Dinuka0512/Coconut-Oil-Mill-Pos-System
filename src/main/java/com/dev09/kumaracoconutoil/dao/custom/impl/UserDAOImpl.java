package com.dev09.kumaracoconutoil.dao.custom.impl;

import com.dev09.kumaracoconutoil.config.FactoryConfiguration;
import com.dev09.kumaracoconutoil.dao.custom.UserDAO;
import com.dev09.kumaracoconutoil.dto.UserDto;
import com.dev09.kumaracoconutoil.dto.extra.UserExtraDto;
import com.dev09.kumaracoconutoil.entity.UserEntity;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.ArrayList;
import java.util.List;

public class UserDAOImpl implements UserDAO {

    @Override
    public void saveAdmin() {
        Session session = FactoryConfiguration.getInstance().getSession();
        try {
            session.beginTransaction();

            Long count = (Long) session
                    .createQuery("SELECT COUNT(u.id) FROM UserEntity u")
                    .uniqueResult();

            if (count > 0) {
                session.getTransaction().commit();
                return;
            }

            UserEntity user = new UserEntity();
            user.setId(generateNewId());
            user.setName("Admin");
            user.setEmail("admin");
            user.setPassword("admin");
            user.setRole("admin");

            session.persist(user);
            session.getTransaction().commit();

        } catch (Exception e) {
            session.getTransaction().rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }
    }

    @Override
    public String generateNewId() {
        Session session = FactoryConfiguration.getInstance().getSession();
        try {
            String lastId = (String) session
                    .createQuery("SELECT u.id FROM UserEntity u ORDER BY u.id DESC")
                    .setMaxResults(1)
                    .uniqueResult();
            if (lastId == null) return "U001";
            int number = Integer.parseInt(lastId.substring(1));
            return String.format("U%03d", number + 1);
        } finally {
            session.close();
        }
    }

    @Override
    public UserExtraDto login(String email, String password) {
        Session session = FactoryConfiguration.getInstance().getSession();
        try {
            session.beginTransaction();

            UserEntity user = (UserEntity) session
                    .createQuery("FROM UserEntity u WHERE u.email = :email")
                    .setParameter("email", email)
                    .uniqueResult();

            session.getTransaction().commit();

            if (user == null) {
                return new UserExtraDto(null, false, "User not found!");
            }

            if (!user.getPassword().equals(password)) {
                return new UserExtraDto(null, false, "Password is wrong!");
            }

            UserDto dto = new UserDto(
                    user.getId(),
                    user.getName(),
                    user.getEmail(),
                    user.getPassword(),
                    user.getRole()
            );
            return new UserExtraDto(dto, true, "Login successful!");

        } catch (Exception e) {
            e.printStackTrace();
            return new UserExtraDto(null, false, "Error occurred");
        } finally {
            session.close();
        }
    }

    @Override
    public boolean updateUser(UserEntity user) {
        Session session = FactoryConfiguration.getInstance().getSession();
        Transaction tx = session.beginTransaction();
        try {
            session.merge(user);
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
            return false;
        } finally {
            session.close();
        }
    }

    @Override
    public ArrayList<UserDto> getAllUsersDto() {
        Session session = FactoryConfiguration.getInstance().getSession();
        ArrayList<UserDto> dtos = new ArrayList<>();
        try {
            List<UserEntity> users = session.createQuery("FROM UserEntity", UserEntity.class).list();
            for (UserEntity u : users) {
                dtos.add(new UserDto(u.getId(), u.getName(), u.getEmail(), u.getPassword(), u.getRole()));
            }
        } finally {
            session.close();
        }
        return dtos;
    }

    @Override
    public boolean saveUser(UserEntity user) {
        Session session = FactoryConfiguration.getInstance().getSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.persist(user);
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
            return false;
        } finally {
            session.close();
        }
    }

    @Override
    public boolean deleteUser(String id) {
        Session session = FactoryConfiguration.getInstance().getSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            UserEntity user = session.get(UserEntity.class, id);
            if (user != null) {
                session.remove(user);
                tx.commit();
                return true;
            }
            return false;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
            return false;
        } finally {
            session.close();
        }
    }

    @Override
    public String generateNewUserId() {
        Session session = FactoryConfiguration.getInstance().getSession();
        try {
            String lastId = (String) session
                    .createQuery("SELECT u.id FROM UserEntity u ORDER BY u.id DESC")
                    .setMaxResults(1)
                    .uniqueResult();
            if (lastId == null) return "U001";
            int number = Integer.parseInt(lastId.substring(1));
            return String.format("U%03d", number + 1);
        } finally {
            session.close();
        }
    }

    @Override
    public boolean isEmailExist(String email, String excludeUserId) {
        Session session = FactoryConfiguration.getInstance().getSession();
        try {
            String hql = "SELECT COUNT(u) FROM UserEntity u WHERE u.email = :email";
            if (excludeUserId != null && !excludeUserId.isEmpty()) {
                hql += " AND u.id != :excludeId";
            }
            var query = session.createQuery(hql, Long.class).setParameter("email", email);
            if (excludeUserId != null && !excludeUserId.isEmpty()) {
                query.setParameter("excludeId", excludeUserId);
            }
            return query.uniqueResult() > 0;
        } finally {
            session.close();
        }
    }

    @Override
    public boolean isNameExist(String name, String excludeUserId) {
        Session session = FactoryConfiguration.getInstance().getSession();
        try {
            String hql = "SELECT COUNT(u) FROM UserEntity u WHERE u.name = :name";
            if (excludeUserId != null && !excludeUserId.isEmpty()) {
                hql += " AND u.id != :excludeId";
            }
            var query = session.createQuery(hql, Long.class).setParameter("name", name);
            if (excludeUserId != null && !excludeUserId.isEmpty()) {
                query.setParameter("excludeId", excludeUserId);
            }
            return query.uniqueResult() > 0;
        } finally {
            session.close();
        }
    }

    @Override
    public UserExtraDto loginByName(String name, String password) {
        Session session = FactoryConfiguration.getInstance().getSession();
        try {
            session.beginTransaction();

            UserEntity user = (UserEntity) session
                    .createQuery("FROM UserEntity u WHERE u.name = :name")
                    .setParameter("name", name)
                    .uniqueResult();

            session.getTransaction().commit();

            if (user == null) {
                return new UserExtraDto(null, false, "User not found!");
            }

            if (!user.getPassword().equals(password)) {
                return new UserExtraDto(null, false, "Password is wrong!");
            }

            UserDto dto = new UserDto(
                    user.getId(),
                    user.getName(),
                    user.getEmail(),
                    user.getPassword(),
                    user.getRole()
            );
            return new UserExtraDto(dto, true, "Login successful!");

        } catch (Exception e) {
            e.printStackTrace();
            return new UserExtraDto(null, false, "Error occurred");
        } finally {
            session.close();
        }
    }

    // ------------ Unused CRUD methods ----------
    @Override public ArrayList<UserEntity> getAll() { return null; }
    @Override public boolean save(UserEntity dto) { return false; }
    @Override public boolean update(UserEntity dto) { return false; }
    @Override public boolean exist(String id) { return false; }
    @Override public boolean delete(String id) { return false; }
    @Override public UserEntity search(String id) { return null; }
}