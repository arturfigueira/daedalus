package com.daedalus.core.data;

class BooleanType implements DataType<Boolean> {

    @Override
    public Boolean parse(Object rawObject) throws IncorrectTypeException {
        Boolean parsed = null;
        if(rawObject instanceof Boolean){
            parsed = (Boolean) rawObject;
        }else if(rawObject instanceof String){
            parsed = this.stringToBoolean((String) rawObject);
        }else if(rawObject instanceof Number){
            parsed = this.numberToBoolean((Number) rawObject);
        }
        return parsed;
    }

    protected boolean stringToBoolean(String rawString) throws IncorrectTypeException {
        boolean parsed = rawString.equalsIgnoreCase("true");
        if(!parsed && !rawString.equalsIgnoreCase("false")){
            parsed = this.numberToBoolean(this.getIntValue(rawString));
        }
        return parsed;
    }

    protected int getIntValue(String rawString) throws IncorrectTypeException {
        try{
            return Integer.parseInt(rawString);
        }catch (NumberFormatException e){
            throw new IncorrectTypeException("Unable to parse "+rawString+" to Boolean");
        }

    }

    protected boolean numberToBoolean(Number value) throws IncorrectTypeException {
        boolean parsed = value.equals(1);
        if(!parsed && !value.equals(0)){
            throw new IncorrectTypeException("Unable to parse "+value+" to boolean");
        }
        return parsed;
    }

    @Override
    public boolean isA(final Object object) {
        return object instanceof Boolean;
    }
}
