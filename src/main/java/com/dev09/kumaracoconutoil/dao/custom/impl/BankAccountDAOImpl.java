package com.dev09.kumaracoconutoil.dao.custom.impl;

import com.dev09.kumaracoconutoil.config.FactoryConfiguration;
import com.dev09.kumaracoconutoil.dao.custom.BankAccountDAO;
import com.dev09.kumaracoconutoil.entity.BankAccountEntity;
import com.dev09.kumaracoconutoil.util.AuditUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class BankAccountDAOImpl implements BankAccountDAO {

    @Override
    public ArrayList<BankAccountEntity> getAll() {
        Session session = FactoryConfiguration.getInstance().getSession();
        try {
            Query<BankAccountEntity> query = session.createQuery(
                    "FROM BankAccountEntity", BankAccountEntity.class);
            return new ArrayList<>(query.list());
        } finally {
            session.close();
        }
    }

    @Override
    public boolean save(BankAccountEntity entity) {
        Session session = FactoryConfiguration.getInstance().getSession();
        Transaction tx = session.beginTransaction();
        try {
            session.persist(entity);
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
    public boolean update(BankAccountEntity entity) {
        Session session = FactoryConfiguration.getInstance().getSession();
        Transaction tx = session.beginTransaction();
        try {
            session.merge(entity);
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
    public boolean exist(String id) {
        Session session = FactoryConfiguration.getInstance().getSession();
        try {
            Query<Long> query = session.createQuery(
                    "SELECT COUNT(b) FROM BankAccountEntity b WHERE b.bankId = :id",
                    Long.class);
            query.setParameter("id", id);
            Long count = query.uniqueResult();
            return count != null && count > 0;
        } finally {
            session.close();
        }
    }

    @Override
    public boolean delete(String id) {
        Session session = FactoryConfiguration.getInstance().getSession();
        Transaction tx = session.beginTransaction();
        try {
            String sql = "UPDATE bank_account SET is_deleted = true, updated_by = :updatedBy, "
                    + "updated_at = :updatedAt WHERE bank_id = :id";
            int rows = session.createNativeQuery(sql)
                    .setParameter("id", id)
                    .setParameter("updatedBy", AuditUtil.currentUser())
                    .setParameter("updatedAt", LocalDateTime.now())
                    .executeUpdate();
            tx.commit();
            return rows > 0;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
            return false;
        } finally {
            session.close();
        }
    }

    @Override
    public String generateNewId() {
        Session session = FactoryConfiguration.getInstance().getSession();
        try {
            NativeQuery<String> query = session.createNativeQuery(
                    "SELECT bank_id FROM bank_account ORDER BY bank_id DESC LIMIT 1", String.class);
            String lastId = query.uniqueResult();

            if (lastId == null || lastId.isEmpty()) {
                return "BANK001";
            }
            String numericPart = lastId.replaceAll("[^0-9]", "");
            if (numericPart.isEmpty()) {
                return "BANK001";
            }
            return String.format("BANK%03d", Integer.parseInt(numericPart) + 1);
        } catch (NumberFormatException e) {
            return "BANK001";
        } finally {
            session.close();
        }
    }

    @Override
    public BankAccountEntity search(String id) {
        Session session = FactoryConfiguration.getInstance().getSession();
        try {
            NativeQuery<BankAccountEntity> query = session.createNativeQuery(
                    "SELECT * FROM bank_account WHERE bank_id = :id AND is_deleted = false",
                    BankAccountEntity.class);
            query.setParameter("id", id);
            return query.uniqueResult();
        } finally {
            session.close();
        }
    }
}
