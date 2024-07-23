/**
 * 
 */
package com.workruit.us.application.enums;

/**
 * @author Mahesh
 *
 */
public enum Citizenship {
	US_CITIZEN, NON_US_CITIZEN,	US_NATIONAL,H1VISA,GREEN_CARD,LAWFUL_PERMANENT_RECIDENSE, ALL;
	
	public int getValue() {
	    return ordinal() + 1;
	}
	
    public String toString(){
        switch(this){
        case US_CITIZEN :
            return "U.S. Citizen";
        case NON_US_CITIZEN :
            return "Non U.S. Citizen";
        case US_NATIONAL :
            return "U.S. National";
        case H1VISA :
            return "H-1 Visa";
        case GREEN_CARD :
            return "Green card";
        case LAWFUL_PERMANENT_RECIDENSE :
            return "Lawful Permanent Resident Alien";
        case ALL :
            return "All";
        }
        return null;
    }

    public static int getValueOf(String value){
        if(value.equalsIgnoreCase(US_CITIZEN.toString()))
            return Citizenship.US_CITIZEN.getValue();
        else if(value.equalsIgnoreCase(NON_US_CITIZEN.toString()))
            return Citizenship.NON_US_CITIZEN.getValue();
        else if(value.equalsIgnoreCase(US_NATIONAL.toString()))
            return Citizenship.US_NATIONAL.getValue();
        else if(value.equalsIgnoreCase(H1VISA.toString()))
            return Citizenship.H1VISA.getValue();
        else if(value.equalsIgnoreCase(GREEN_CARD.toString()))
            return Citizenship.GREEN_CARD.getValue();
        else if(value.equalsIgnoreCase(LAWFUL_PERMANENT_RECIDENSE.toString()))
            return Citizenship.LAWFUL_PERMANENT_RECIDENSE.getValue();
        else if(value.equalsIgnoreCase(ALL.toString()))
            return Citizenship.ALL.getValue();
        else
            return -1;
    }

    public static Citizenship getValueOf(int value) {
        switch (value) {
            case 1:
                return US_CITIZEN;
            case 2:
                return NON_US_CITIZEN;
            case 3:
                return US_NATIONAL;
            case 4:
                return H1VISA;
            case 5:
                return GREEN_CARD;
            case 6:
                return LAWFUL_PERMANENT_RECIDENSE;
            case 7:
                return ALL;
            default:
                return null;
        }
    }

}
