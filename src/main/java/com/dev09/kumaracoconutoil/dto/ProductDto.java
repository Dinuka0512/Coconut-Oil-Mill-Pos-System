package com.dev09.kumaracoconutoil.dto;

import com.dev09.kumaracoconutoil.util.AuditFormat;
import com.dev09.kumaracoconutoil.dto.ProductPriceDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Locale;

public class ProductDto {
    private String id;
    private String itemcode;
    private String name;
    private double qty;
    private double price;
    private ArrayList<Double> prices = new ArrayList<>();
    private ArrayList<ProductPriceDto> priceDetails = new ArrayList<>();
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;

    public ProductDto() {}

    public ProductDto(String id, String itemcode, String name, double qty, double price) {
        this.id = id;
        this.itemcode = itemcode;
        this.name = name;
        this.qty = qty;
        this.price = price;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getItemcode() { return itemcode; }
    public void setItemcode(String itemcode) { this.itemcode = itemcode; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getQty() { return qty; }
    public void setQty(double qty) { this.qty = qty; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public ArrayList<Double> getPrices() { return prices; }
    public void setPrices(ArrayList<Double> prices) { this.prices = prices == null ? new ArrayList<>() : prices; }

    /**
     * Price rows (price + stock) for this product, e.g. [[price:100, stock:10], [price:20, stock:5]].
     */
    public ArrayList<ProductPriceDto> getPriceDetails() { return priceDetails; }
    public void setPriceDetails(ArrayList<ProductPriceDto> priceDetails) { this.priceDetails = priceDetails == null ? new ArrayList<>() : priceDetails; }

    public ArrayList<ProductPriceDto> getPriceStockPairs() { return priceDetails; }

    public String getPriceArray() {
        if (prices == null || prices.isEmpty()) {
            return "[" + formatPrice(price) + "]";
        }
        StringBuilder builder = new StringBuilder("[");
        for (int i = 0; i < prices.size(); i++) {
            if (i > 0) builder.append(", ");
            builder.append(formatPrice(prices.get(i)));
        }
        return builder.append("]").toString();
    }

    private String formatPrice(double value) {
        if (value == Math.rint(value)) {
            return String.valueOf((long) value);
        }
        return String.format(Locale.US, "%.2f", value);
    }

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
        return "ProductDto{id=" + id + ", itemcode=" + itemcode + ", name=" + name + ", qty=" + qty + ", price=" + price + "}";
    }
}
