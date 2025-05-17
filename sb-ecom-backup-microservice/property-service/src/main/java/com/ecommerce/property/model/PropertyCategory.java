package com.ecommerce.property.model;

public enum PropertyCategory {
    NHA_RIENG(1L, "Nhà riêng"),
    CHUNG_CU(2L, "Chung cư"),
    BIET_THU(3L, "Biệt thự"),
    CAN_HO(4L, "Căn hộ"),
    NHA_PHO(5L, "Nhà phố"),
    DAT_NEN(6L, "Đất nền"),
    VAN_PHONG(7L, "Văn phòng"),
    MAT_BANG(8L, "Mặt bằng kinh doanh");

    private final Long id;
    private final String name;

    PropertyCategory(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public static PropertyCategory fromId(Long id) {
        if (id == null) {
            return null;
        }
        
        for (PropertyCategory category : PropertyCategory.values()) {
            if (category.getId().equals(id)) {
                return category;
            }
        }
        return null;
    }
} 