/*
 *
 *  * JBoss, Home of Professional Open Source.
 *  * Copyright 2011, Red Hat, Inc., and individual contributors
 *  * as indicated by the @author tags. See the copyright.txt file in the
 *  * distribution for a full listing of individual contributors.
 *  *
 *  * This is free software; you can redistribute it and/or modify it
 *  * under the terms of the GNU Lesser General Public License as
 *  * published by the Free Software Foundation; either version 2.1 of
 *  * the License, or (at your option) any later version.
 *  *
 *  * This software is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  * Lesser General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU Lesser General Public
 *  * License along with this software; if not, write to the Free
 *  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 *  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 */

package org.jboss.capedwarf.datastore.query;

import java.util.Collection;
import java.util.Date;
import java.util.Map;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.Category;
import com.google.appengine.api.datastore.Email;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.api.datastore.IMHandle;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Link;
import com.google.appengine.api.datastore.PhoneNumber;
import com.google.appengine.api.datastore.PostalAddress;
import com.google.appengine.api.datastore.Rating;
import com.google.appengine.api.datastore.ShortBlob;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.users.User;
import org.apache.lucene.document.Document;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.LuceneOptions;
import org.hibernate.search.bridge.TwoWayFieldBridge;
import org.hibernate.search.bridge.impl.BridgeFactory;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class PropertyMapBridge implements FieldBridge {

    private static final String NULL_TOKEN = "__capedwarf___NULL___";

    public static final TwoWayFieldBridge TEXT_BRIDGE = new TextBridge();
    public static final TwoWayFieldBridge PHONE_NUMBER_BRIDGE = new PhoneNumberBridge();
    public static final TwoWayFieldBridge POSTAL_ADDRESS_BRIDGE = new PostalAddressBridge();
    public static final TwoWayFieldBridge EMAIL_BRIDGE = new EmailBridge();
    public static final TwoWayFieldBridge USER_BRIDGE = new UserBridge();
    public static final TwoWayFieldBridge LINK_BRIDGE = new LinkBridge();
    public static final TwoWayFieldBridge KEY_BRIDGE = new KeyBridge();
    public static final TwoWayFieldBridge RATING_BRIDGE = new RatingBridge();
    public static final TwoWayFieldBridge GEO_PT_BRIDGE = new GeoPtBridge();
    public static final TwoWayFieldBridge CATEGORY_BRIDGE = new CategoryBridge();
    public static final TwoWayFieldBridge IM_HANDLE_BRIDGE = new IMHandleBridge();
    public static final TwoWayFieldBridge BLOB_KEY_BRIDGE = new BlobKeyBridge();
    public static final TwoWayFieldBridge BLOB_BRIDGE = new BlobBridge();
    public static final TwoWayFieldBridge SHORT_BLOB_BRIDGE = new ShortBlobBridge();
    public static final LongBridge LONG_BRIDGE = new LongBridge();
    public static final DoubleBridge DOUBLE_BRIDGE = new DoubleBridge();

    public static final String UNINDEXED_VALUE_CLASS_NAME = Entity.class.getName() + "$UnindexedValue";

    public void set(String name, Object value, Document document, LuceneOptions luceneOptions) {
        Map<String, ?> entityProperties = (Map<String, ?>) value;
        for (Map.Entry<String, ?> entry : entityProperties.entrySet()) {
            String propertyName = entry.getKey();
            Object propertyValue = entry.getValue();
            if (!isUnindexedProperty(propertyValue)) {
                if (propertyValue instanceof Collection) {
                    Collection collection = (Collection) propertyValue;
                    for (Object element : collection) {
                        luceneOptions.addFieldToDocument(propertyName, convertToString(element), document);
                    }
                } else {
                    luceneOptions.addFieldToDocument(propertyName, convertToString(propertyValue), document);
                }
            }
        }
    }

    private boolean isUnindexedProperty(Object value) {
        return value != null && UNINDEXED_VALUE_CLASS_NAME.equals(value.getClass().getName());
    }

    public String convertToString(Object value) {
        if (value == null) {
            return NULL_TOKEN;
        }

        if (value instanceof String) {
            return String.valueOf(value);
        } else if (value instanceof Boolean) {
            return BridgeFactory.BOOLEAN.objectToString(value);
        } else if (value instanceof Float || value instanceof Double) {
            return DOUBLE_BRIDGE.objectToString(value);
        } else if (value instanceof Number) {
            return LONG_BRIDGE.objectToString(value);
        } else if (value instanceof Date) {
            return BridgeFactory.DATE_MILLISECOND.objectToString(value);
        } else if (value instanceof Text) {
            return TEXT_BRIDGE.objectToString(value);
        } else if (value instanceof PhoneNumber) {
            return PHONE_NUMBER_BRIDGE.objectToString(value);
        } else if (value instanceof PostalAddress) {
            return POSTAL_ADDRESS_BRIDGE.objectToString(value);
        } else if (value instanceof Email) {
            return EMAIL_BRIDGE.objectToString(value);
        } else if (value instanceof User) {
            return USER_BRIDGE.objectToString(value);
        } else if (value instanceof Link) {
            return LINK_BRIDGE.objectToString(value);
        } else if (value instanceof Key) {
            return KEY_BRIDGE.objectToString(value);
        } else if (value instanceof Rating) {
            return RATING_BRIDGE.objectToString(value);
        } else if (value instanceof GeoPt) {
            return GEO_PT_BRIDGE.objectToString(value);
        } else if (value instanceof Category) {
            return CATEGORY_BRIDGE.objectToString(value);
        } else if (value instanceof IMHandle) {
            return IM_HANDLE_BRIDGE.objectToString(value);
        } else if (value instanceof BlobKey) {
            return BLOB_KEY_BRIDGE.objectToString(value);
        } else if (value instanceof Blob) {
            return BLOB_BRIDGE.objectToString(value);
        } else if (value instanceof ShortBlob) {
            return SHORT_BLOB_BRIDGE.objectToString(value);
        }
        throw new IllegalArgumentException("Cannot convert value to string. Value was " + value);
    }


    private static class DoubleBridge extends ObjectToStringFieldBridge {
        public String objectToString(Object object) {
            return double2sortableStr(((Number)object).doubleValue());
        }

        public static String double2sortableStr(double val) {
            long f = Double.doubleToRawLongBits(val);
            if (f<0) f ^= 0x7fffffffffffffffL;
            return long2sortableStr(f);
        }

        public static String long2sortableStr(long val) {
            char[] arr = new char[5];
            long2sortableStr(val,arr,0);
            return new String(arr,0,5);
        }

        // uses binary representation of an int to build a string of
        // chars that will sort correctly.  Only char ranges
        // less than 0xd800 will be used to avoid UCS-16 surrogates.
        // we can use the lowest 15 bits of a char, (or a mask of 0x7fff)
        public static int long2sortableStr(long val, char[] out, int offset) {
            val += Long.MIN_VALUE;
            out[offset++] = (char)(val >>>60);
            out[offset++] = (char)(val >>>45 & 0x7fff);
            out[offset++] = (char)(val >>>30 & 0x7fff);
            out[offset++] = (char)(val >>>15 & 0x7fff);
            out[offset] = (char)(val & 0x7fff);
            return 5;
        }
    }


    private static class LongBridge extends ObjectToStringFieldBridge {

        private static final int RADIX = 36;

        private static final char NEGATIVE_PREFIX = '-';

        // NB: NEGATIVE_PREFIX must be < POSITIVE_PREFIX
        private static final char POSITIVE_PREFIX = '0';

        //NB: this must be less than
        /**
         * Equivalent to longToString(Long.MIN_VALUE)
         */
        public static final String MIN_STRING_VALUE = NEGATIVE_PREFIX + "0000000000000";
        /**
         * Equivalent to longToString(Long.MAX_VALUE)
         */
        public static final String MAX_STRING_VALUE = POSITIVE_PREFIX + "1y2p0ij32e8e7";

        /**
         * the length of (all) strings returned by [EMAIL PROTECTED] #longToString}
         */
        public static final int STR_SIZE = MIN_STRING_VALUE.length();

        public String objectToString(Object object) {
            long num = ((Number) object).longValue();
            return longToString(num);
        }

        /**
         * Converts a long to a String suitable for indexing.
         */
        public static String longToString(long num) {

            if (num == Long.MIN_VALUE) {
                // special case, because long is not symetric around zero
                return MIN_STRING_VALUE;
            }

            StringBuilder buf = new StringBuilder(STR_SIZE);

            if (num < 0) {
                buf.append(NEGATIVE_PREFIX);
                num = Long.MAX_VALUE + num + 1;
            } else {
                buf.append(POSITIVE_PREFIX);
            }
            String numStr = Long.toString(num, RADIX);

            int padLen = STR_SIZE - numStr.length() - buf.length();
            while (padLen-- > 0) {
                buf.append('0');
            }
            buf.append(numStr);

            return buf.toString();
        }

    }

    private static class TextBridge extends ObjectToStringFieldBridge {
        public String objectToString(Object object) {
            return ((Text) object).getValue();
        }
    }

    private static class PhoneNumberBridge extends ObjectToStringFieldBridge {
        public String objectToString(Object object) {
            return ((PhoneNumber) object).getNumber();
        }
    }

    private static class PostalAddressBridge extends ObjectToStringFieldBridge {
        public String objectToString(Object object) {
            return ((PostalAddress) object).getAddress();
        }
    }

    private static class EmailBridge extends ObjectToStringFieldBridge {
        public String objectToString(Object object) {
            return ((Email) object).getEmail();
        }
    }

    private static class UserBridge extends ObjectToStringFieldBridge {
        public String objectToString(Object object) {
            return ((User) object).getEmail();    // TODO: add other properties
        }
    }

    private static class LinkBridge extends ObjectToStringFieldBridge {
        public String objectToString(Object object) {
            return ((Link) object).getValue();
        }
    }

    private static class KeyBridge extends ObjectToStringFieldBridge {
        private GAEKeyTransformer keyTransformer = new GAEKeyTransformer();

        public String objectToString(Object object) {
            return keyTransformer.toString((Key) object);
        }
    }

    private static class RatingBridge extends ObjectToStringFieldBridge {
        public String objectToString(Object object) {
            return BridgeFactory.INTEGER.objectToString(((Rating) object).getRating());
        }
    }

    private static class GeoPtBridge extends ObjectToStringFieldBridge {
        public String objectToString(Object object) {
            return BridgeFactory.FLOAT.objectToString(((GeoPt) object).getLatitude())
                    + ";"
                    + BridgeFactory.FLOAT.objectToString(((GeoPt) object).getLongitude());
        }
    }

    private static class CategoryBridge extends ObjectToStringFieldBridge {
        public String objectToString(Object object) {
            return ((Category) object).getCategory();
        }
    }

    private static class IMHandleBridge extends ObjectToStringFieldBridge {
        public String objectToString(Object object) {
            return ((IMHandle) object).getProtocol()
                    + ";"
                    + ((IMHandle) object).getAddress();
        }
    }

    private static class BlobKeyBridge extends ObjectToStringFieldBridge {
        public String objectToString(Object object) {
            return ((BlobKey) object).getKeyString();
        }
    }

    private static class BlobBridge extends ObjectToStringFieldBridge {
        public String objectToString(Object object) {
            byte[] bytes = ((Blob) object).getBytes();
            return bytesToString(bytes);
        }
    }

    private static class ShortBlobBridge extends ObjectToStringFieldBridge {
        public String objectToString(Object object) {
            byte[] bytes = ((ShortBlob) object).getBytes();
            return bytesToString(bytes);
        }
    }

    private static String bytesToString(byte bytes[]) {
        // TODO: This impl is temporary. Find better one.
        StringBuilder sbuf = new StringBuilder();
        for (byte aByte : bytes) {
            String hex = Integer.toString(aByte, 16);
            String twoCharHex = (hex.length() == 1 ? "0" : "") + hex;
            sbuf.append(twoCharHex);
        }
        return sbuf.toString();
    }

    /**
     *
     */
    public abstract static class ObjectToStringFieldBridge implements TwoWayFieldBridge {
        public Object get(String name, Document document) {
            throw new UnsupportedOperationException("should not be called");
        }

        public void set(String name, Object value, Document document, LuceneOptions luceneOptions) {
            throw new UnsupportedOperationException("should not be called");
        }
    }
}

