package com.rohan.orderflow.inventory;

import com.rohan.orderflow.product.Product;
import jakarta.persistence.*;

@Entity
@Table(name = "inventory")
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "product_id",
            nullable = false,
            unique = true
    )
    private Product product;

    @Column(name = "available_quantity", nullable = false)
    private int availableQuantity;

    @Column(name = "reserved_quantity", nullable = false)
    private int reservedQuantity;

    @Version
    private Long version;

    protected Inventory() {
    }

    public void addStock(int quantity) {
    this.availableQuantity += quantity;
}

public void reserve(int quantity) {

    if (availableQuantity < quantity) {
        throw new InsufficientInventoryException(
                product.getId(),
                availableQuantity,
                quantity
        );
    }

    this.availableQuantity -= quantity;
    this.reservedQuantity += quantity;
}

    public Inventory(
            Product product,
            int availableQuantity
    ) {
        this.product = product;
        this.availableQuantity = availableQuantity;
        this.reservedQuantity = 0;
    }

    public Long getId() {
        return id;
    }

    public Product getProduct() {
        return product;
    }

    public int getAvailableQuantity() {
        return availableQuantity;
    }

    public int getReservedQuantity() {
        return reservedQuantity;
    }

    public Long getVersion() {
        return version;
    }
}