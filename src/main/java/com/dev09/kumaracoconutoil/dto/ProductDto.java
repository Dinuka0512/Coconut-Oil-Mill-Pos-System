package com.dev09.kumaracoconutoil.dto;

import com.dev09.kumaracoconutoil.util.AuditFormat;
import com.dev09.kumaracoconutoil.util.PriceQtyArray;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class ProductDto {
    private String id;
    private String itemcode;
    private String name;
    private String supplierId;

    /** Each row is {price, qty}. */
    private double[][] priceQty = new double[0][];

    private boolean isDeleted;
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;

    public ProductDto() {}

    public ProductDto(String id, String itemcode, String name, double[][] priceQty) {
        this.id = id;
        this.itemcode = itemcode;
        this.name = name;
        setPriceQty(priceQty);
    }

    /** Convenience constructor for a single price/qty pair. */
    public ProductDto(String id, String itemcode, String name, double qty, double price) {
        this(id, itemcode, name, new double[][]{{price, qty}});
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getItemcode() { return itemcode; }
    public void setItemcode(String itemcode) { this.itemcode = itemcode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSupplierId() { return supplierId; }
    public void setSupplierId(String supplierId) { this.supplierId = supplierId; }

    public double[][] getPriceQty() { return priceQty; }
    public void setPriceQty(double[][] priceQty) { this.priceQty = PriceQtyArray.normalize(priceQty); }

    public double[][] getPriceQtyPairs() { return getPriceQty(); }

    /** Total stock, derived from the 2D array. */
    public double getQty() { return PriceQtyArray.totalQty(priceQty); }

    public void setQty(double qty) { setPriceQty(PriceQtyArray.withTotalQty(priceQty, qty)); }

    /** Primary price, derived from the first row of the 2D array. */
    public double getPrice() { return PriceQtyArray.primaryPrice(priceQty); }

    public void setPrice(double price) {
        if (priceQty.length == 0) {
            setPriceQty(new double[][]{{price, 0.0}});
        } else {
            priceQty[0][PriceQtyArray.PRICE] = price;
        }
    }

    public ArrayList<Double> getPrices() { return PriceQtyArray.prices(priceQty); }

    public String getPriceArray() { return PriceQtyArray.formatPrices(priceQty); }

    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean deleted) { isDeleted = deleted; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getCreatedInfo() { return AuditFormat.info(createdBy, createdAt); }
    public String getUpdatedInfo() { return AuditFormat.info(updatedBy, updatedAt); }

    @Override
    public String toString() {
        return "ProductDto{id=" + id + ", itemcode=" + itemcode + ", name=" + name
                + ", priceQty=" + PriceQtyArray.toJson(priceQty) + "}";
    }
}
