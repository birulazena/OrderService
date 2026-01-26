package com.github.birulazena.OrderService.entity;

public enum Status {
    CREATED,
    PAID,
    COMPLETED,
    CANCELLED;

    public static boolean isValid(String status) {
        if (status == null)
            return false;

        try {
            Status.valueOf(status.toUpperCase());
            return true;
        } catch (IllegalArgumentException ex){
            return false;
        }
    }

}
