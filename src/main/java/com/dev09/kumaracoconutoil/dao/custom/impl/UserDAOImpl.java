package com.dev09.kumaracoconutoil.dao.custom.impl;

import com.dev09.kumaracoconutoil.config.FactoryConfiguration;
import com.dev09.kumaracoconutoil.dao.custom.UserDAO;
import com.dev09.kumaracoconutoil.dto.UserDto;
import com.dev09.kumaracoconutoil.dto.extra.UserExtraDto;
import com.dev09.kumaracoconutoil.entity.UserEntity;
import jakarta.persistence.Query;
import org.hibernate.Session;

import java.util.ArrayList;

public class UserDAOImpl implements UserDAO {
    //SAVE ADMIN---
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
                return;
            }

            // if No users → create admin
            UserEntity user = new UserEntity();
            user.setId(generateNewId());
            user.setName("Admin");
            user.setEmail("admin@gmail.com");
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

    // GENERATE NEW ID---
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

    //LOGIN---
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

            // User not found
            if (user == null) {
                return new UserExtraDto(null, false, "User not found, \nSomething went wrong check your entered email..");
            }

            // Wrong password
            if (!user.getPassword().equals(password)) {
                return new UserExtraDto(null, false, "Password is Wrong!");
            }

            // Login success
            UserDto dto = new UserDto(
                    user.getId(),
                    user.getName(),
                    user.getEmail(),
                    user.getPassword()
            );

            return new UserExtraDto(dto, true, "Login successful!");

        } catch (Exception e) {
            e.printStackTrace();
            return new UserExtraDto(null, false, "Error occurred");

        } finally {
            session.close();
        }
    }



    // ------------ Not Implemented -----

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
    public UserEntity search(String id) {
        return null;
    }
}
