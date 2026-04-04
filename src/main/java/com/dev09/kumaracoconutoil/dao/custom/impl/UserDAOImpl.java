package com.dev09.kumaracoconutoil.dao.custom.impl;

import com.dev09.kumaracoconutoil.config.FactoryConfiguration;
import com.dev09.kumaracoconutoil.dao.custom.UserDAO;
import com.dev09.kumaracoconutoil.entity.UserEntity;
import jakarta.persistence.Query;
import org.hibernate.Session;

import java.util.ArrayList;

public class UserDAOImpl implements UserDAO {
    @Override
    public ArrayList<UserEntity> getAll() {
        return null;
    }

    @Override
    public boolean save(UserEntity dto) {
        return false;
    }

    @Override
    public boolean update(UserEntity dto) {
        return false;
    }

    @Override
    public boolean exist(String id) {
        return false;
    }

    @Override
    public boolean delete(String id) {
        return false;
    }

    @Override
    public String generateNewId() {
        Session session = FactoryConfiguration.getInstance().getSession();
        String lastId = (String) session
                .createQuery("SELECT u.id FROM UserEntity u ORDER BY u.id DESC")
                .setMaxResults(1)
                .uniqueResult();

        session.close();

        if (lastId == null) {
            return "U001"; // first ID
        }

        // Remove "U" and convert number
        int number = Integer.parseInt(lastId.substring(1));
        number++;

        // Format with leading zeros
        return String.format("U%03d", number);
    }

    @Override
    public UserEntity search(String id) {
        return null;
    }

    @Override
    public void saveAdmin() {
        Session session = FactoryConfiguration.getInstance().getSession();

        try {
            session.beginTransaction();

            //---check is Users are have
            Long count = (Long) session
                    .createQuery("SELECT COUNT(u.id) FROM UserEntity u")
                    .uniqueResult();

            if (count > 0) {
                // Users already exist → do NOT create admin
                session.getTransaction().commit();
            }

            // if No users → create admin
            UserEntity user = new UserEntity();
            user.setId(generateNewId());
            user.setName("Admin");
            user.setEmail("Admin@gmail.com");
            user.setPassword("admin");

            session.persist(user);

            session.getTransaction().commit();

        } catch (Exception e) {
            session.getTransaction().rollback();
            e.printStackTrace();

        } finally {
            session.close();
        }
    }
}
