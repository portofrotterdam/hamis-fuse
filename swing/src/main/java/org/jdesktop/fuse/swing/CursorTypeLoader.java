package org.jdesktop.fuse.swing;

import java.awt.Cursor;
import java.lang.reflect.Field;
import java.util.Map;

import org.jdesktop.fuse.TypeLoader;
import org.jdesktop.fuse.TypeLoadingException;

class CursorTypeLoader extends TypeLoader<Cursor> {
    CursorTypeLoader() {
        super(Cursor.class);
    }

    @Override
    public Cursor loadType(String name, String value, Class<?> resolver, Map<String, Object> properties) {
        String cursorValue = value.trim().replace(' ', '_').toUpperCase();
        
        if (!cursorValue.endsWith("_CURSOR")) {
            cursorValue += "_CURSOR";
        }
        
        Field cursorField = null;
        try {
            cursorField = Cursor.class.getField(cursorValue);
        } catch (SecurityException e) {
        } catch (NoSuchFieldException e) {
            try {
                Cursor c = Cursor.getSystemCustomCursor(value);
                if (c == null) {
                    throw new TypeLoadingException("Theme resource " + name +
                                                   " is not a valid cursor. " +
                                                   "The cursor " + value + " does not exist.");
                }
                return c;
            } catch (Exception e1) {
                throw new TypeLoadingException("Theme resource " + name +
                                               " is not a valid cursor. " +
                                               "The cursor " + value + " does not exist.", e1);
            }
        }

        try {
            return Cursor.getPredefinedCursor((Integer) cursorField.get(null));
        } catch (Exception e) {
            throw new TypeLoadingException("Theme resource " + name +
                                           " is not a valid cursor. " +
                                           "The cursor " + cursorValue + " does not exist.");
        }
    }
}
