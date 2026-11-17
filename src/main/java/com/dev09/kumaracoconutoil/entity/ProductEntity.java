package com.dev09.kumaracoconutoil.entity;

import com.dev09.kumaracoconutoil.util.PriceQtyArray;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "product")
@SQLDelete(sql = "UPDATE product SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
public class ProductEntity extends AuditableEntity {

    @Id
    private String id;

    @Column(unique = true)
    private String itemcode;

    private String name;

    /** JSON text: [[price, qty], [price, qty], ...] */
    @Column(name = "price_qty", columnDefinition = "TEXT")
    private String priceQty;

    @Column(name = "is_deleted", columnDefinition = "boolean default false")
    private boolean isDeleted = false;

    public ProductEntity() {}

    public ProductEntity(String id, String itemcode, String name, double[][] priceQty) {
        this(id, itemcode, name, priceQty, false);
    }

    public ProductEntity(String id, String itemcode, String name, double[][] priceQty, boolean isDeleted) {
        this.id = id;
        this.itemcode = itemcode;
        this.name = name;
        setPriceQtyPairs(priceQty);
        this.isDeleted = isDeleted;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getItemcode() { return itemcode; }
    public void setItemcode(String itemcode) { this.itemcode = itemcode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPriceQty() { return priceQty; }
    public void setPriceQty(String priceQty) { this.priceQty = priceQty; }

    public double[][] getPriceQtyPairs() { return PriceQtyArray.fromJson(priceQty); }
    public void setPriceQtyPairs(double[][] pairs) { this.priceQty = PriceQtyArray.toJson(pairs); }

    /** Total stock, derived from the 2D array. */
    public double getQty() { return PriceQtyArray.totalQty(getPriceQtyPairs()); }

    /** Adjust total stock by changing the first tier, keeping the other tiers intact. */
    public void setQty(double qty) {
        setPriceQtyPairs(PriceQtyArray.withTotalQty(getPriceQtyPairs(), qty));
    }

    /** Primary price, derived from the first row of the 2D array. */
    public double getPrice() { return PriceQtyArray.primaryPrice(getPriceQtyPairs()); }

    public void setPrice(double price) {
        double[][] pairs = getPriceQtyPairs();
        if (pairs.length == 0) {
            pairs = new double[][]{{price, 0.0}};
        } else {
            pairs[0][PriceQtyArray.PRICE] = price;
        }
        setPriceQtyPairs(pairs);
    }

    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean deleted) { isDeleted = deleted; }

    @Override
    public String toString() {
        return "ProductEntity{id=" + id + ", itemcode=" + itemcode + ", name=" + name
                + ", priceQty=" + priceQty + "}";
    }
}
