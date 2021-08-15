package com.github.daedalus.core.elastic.data;

import com.github.daedalus.core.elastic.IncorrectTypeException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor()
public class DateType implements DataType<Date>, Formatable, ZoneTimeable, Localizable {
  private Locale locale = Locale.getDefault();
  private TimeZone timeZone = TimeZone.getDefault();
  private String dateFormat = "yyyy-MM-dd HH:mm:ss";

  @Override
  public Date parse(Object rawObject) throws IncorrectTypeException {
    Date date = null;
    if(rawObject != null){
      if (rawObject instanceof Long) {
        date = new Date((long) rawObject);
      } else if (rawObject instanceof Date) {
        date = new Date(((Date) rawObject).getTime());
      } else if (rawObject instanceof String) {
        date = parseFromString((String) rawObject);
      } else {
        throw new IncorrectTypeException(
            "Unable to parse " + rawObject + " to " + this.getClass().getSimpleName());
      }
    }
    return date;
  }

  protected Date parseFromString(String rawObject) throws IncorrectTypeException {
    Date date;
    try {
      final var simpleDateFormat = new SimpleDateFormat(this.dateFormat, this.locale);
      simpleDateFormat.setTimeZone(timeZone);
      date = simpleDateFormat.parse(rawObject);
    } catch (ParseException e) {
      throw new IncorrectTypeException(e.getMessage());
    }
    return date;
  }

  @Override
  public boolean isA(final Object object) {
    return object instanceof Date;
  }

  @Override
  public void setFormat(String format) {
    this.dateFormat = format;
  }

  @Override
  public void setLocale(Locale locale) {
    this.locale = locale;
  }

  @Override
  public void setTimeZone(TimeZone timeZone) {
    this.timeZone = timeZone;
  }
}
