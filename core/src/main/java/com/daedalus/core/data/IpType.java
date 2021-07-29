package com.daedalus.core.data;

import org.apache.commons.validator.routines.InetAddressValidator;

class IpType implements DataType<String> {

    @Override
    public String parse(Object rawObject) throws IncorrectTypeException {
        String ip = null;
        if(rawObject != null){
            if(!isA(rawObject)){
                throw new IncorrectTypeException("Unable to parse "+rawObject+" to IP");
            }
            ip = (String) rawObject;
        }

        return ip;
    }

    @Override
    public boolean isA(final Object object) {
        return object instanceof String && isAnIp(object.toString());
    }

    private boolean isAnIp(String data) {
        var validator = InetAddressValidator.getInstance();
        return validator.isValidInet4Address(data) || validator.isValidInet6Address(data);
    }
}