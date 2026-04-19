package com.dev09.kumaracoconutoil.dao.custom.impl;

import com.dev09.kumaracoconutoil.config.FactoryConfiguration;
import com.dev09.kumaracoconutoil.dao.custom.UserDAO;
import com.dev09.kumaracoconutoil.dto.UserDto;
import com.dev09.kumaracoconutoil.dto.extra.UserExtraDto;
import com.dev09.kumaracoconutoil.entity.UserEntity;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.ArrayList;

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
            user.setPassword("admin123");
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

    // ------------ Unused CRUD methods ----------
    @Override public ArrayList<UserEntity> getAll() { return null; }
    @Override public boolean save(UserEntity dto) { return false; }
    @Override public boolean update(UserEntity dto) { return false; }
    @Override public boolean exist(String id) { return false; }
    @Override public boolean delete(String id) { return false; }
    @Override public UserEntity search(String id) { return null; }
}