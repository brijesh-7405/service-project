package com.workruit.us.application.enums;

public enum QuestionType {

    RADIO, MULTI_SELECT;

    public String toString(){
        switch(this){
            case RADIO :
                return "Radio";
            case MULTI_SELECT :
                return "multi-select";
        }
        return null;
    }
}
