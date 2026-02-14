package com.codeforge.user_service.exception;

public class CustomerNotFoundException extends  Exception{
    public CustomerNotFoundException(String mess){
        super(mess);
    }
}
