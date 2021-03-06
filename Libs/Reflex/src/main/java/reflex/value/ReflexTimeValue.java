/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2011-2016 Incapture Technologies LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package reflex.value;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import reflex.ReflexException;

/**
 * A wrapper around a Time value that is a first class type in Reflex
 * 
 * @author amkimian
 * 
 */
public class ReflexTimeValue {
    private Date date;

    public ReflexTimeValue() {
        date = new Date();
    }

    public ReflexTimeValue(Date initial) {
        this.date = initial;
    }
    
    public ReflexTimeValue(long initial) {
    	this.date = new Date(initial);
    }

    public ReflexTimeValue(ReflexTimeValue other) {
        this.date = other.date;
    }

    public ReflexTimeValue(String HHmmSS) {
        // Given a hhhmmss, convert to a date (and time)

        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        try {
            date = dateFormat.parse(HHmmSS);
        } catch (ParseException e) {
            throw new ReflexException(-1, "Bad time format - " + e.getMessage());
        }
    }

    public String toString() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        return dateFormat.format(date);
    }

    public long getEpoch() {
        return date.getTime();
    }
    
    public Boolean greaterThanEquals(ReflexTimeValue other) {
        return this.getEpoch() >= other.getEpoch();
    }

    public Boolean greaterThan(ReflexTimeValue other) {
        return this.getEpoch() > other.getEpoch();
    }

    public Boolean lessThanEquals(ReflexTimeValue other) {
        return this.getEpoch() <= other.getEpoch();
    }

    public Boolean lessThan(ReflexTimeValue other) {
        return this.getEpoch() < other.getEpoch();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 7;
        result = prime * result + ((date == null) ? 0 : date.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        ReflexTimeValue that = null;
        if ((obj != null) && (obj instanceof ReflexValue)) {
            that = ((ReflexValue) obj).asTime();
            return this.getEpoch() == that.getEpoch();
        }
        return false;
    }

}
