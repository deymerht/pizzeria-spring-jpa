package com.deymer.pizzeria.service.exeption;

public class EmailApiException extends RuntimeException{
    public EmailApiException(){
        super("Error sending email...");
    }
}
