package com.daedalus.core.data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

class DateType implements DataType<Date> {

    @Override
    public Date parse(Object rawObject) throws IncorrectTypeException {
        Date date = null;
        if(rawObject instanceof Long){
            date = new Date((long) rawObject);
        }else if(rawObject instanceof Date){
            date =  new Date(((Date)rawObject).getTime());
        }else if(rawObject instanceof String){
            date = parseFromString((String) rawObject);
        }
        return date;
    }

    protected Date parseFromString(String rawObject) throws IncorrectTypeException {
        Date date;
        try {
            date = SimpleDateFormat.getDateInstance().parse(rawObject); //FIXME: Make it configurable
        } catch (ParseException e) {
           throw new IncorrectTypeException(e.getMessage());
        }
        return date;
    }

    @Override
    public boolean isA(final Object object) {
        return object instanceof Date;
    }
}