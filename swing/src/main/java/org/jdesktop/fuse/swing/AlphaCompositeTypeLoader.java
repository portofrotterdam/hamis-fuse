package org.jdesktop.fuse.swing;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.lang.reflect.Field;
import java.util.Map;

import org.jdesktop.fuse.TypeLoader;
import org.jdesktop.fuse.TypeLoadingException;

class AlphaCompositeTypeLoader extends TypeLoader<Composite> {
    AlphaCompositeTypeLoader() {
        super(Composite.class, AlphaComposite.class);
    }

    @Override
    public AlphaComposite loadType(String name, String value, Class<?> resolver, Map<String, Object> properties) {
        float alphaValue = 1.0f;
        String[] parts = value.split(",");
        String compositeValue = parts[0].trim().replace(' ', '_').toUpperCase();
        
        Field compositeField = null;
        try {
            compositeField = AlphaComposite.class.getField(compositeValue);
        } catch (SecurityException e) {
        } catch (NoSuchFieldException e) {
            throw new TypeLoadingException("Theme resource " + name +
                                           " is not a valid alpha composite. " +
                                           "The composite " + value + " does not exist.");
        }
        
        if (compositeField.getType() != int.class) {
            throw new TypeLoadingException("Theme resource " + name +
                                           " is not a valid alpha composite. " +
                                           "The composite " + value + " does not exist.");
        }
        
        if (parts.length == 2) {
            try {
                alphaValue = Float.parseFloat(parts[1].trim());
            } catch (NumberFormatException e) {
                throw new TypeLoadingException("Theme resource " + name +
                                               " is not a valid alpha composite. " +
                                               "The alpha value " + value + " is not a float.");
            }
        }
        
        if (alphaValue < 0.0f || alphaValue > 1.0f) {
            throw new TypeLoadingException("Theme resource " + name +
                                           " is not a valid alpha composite. " +
                                           "The alpha value " + value + " must be >= 0.0f and <= 1.0f.");
        }

        try {
            return AlphaComposite.getInstance((Integer) compositeField.get(null), alphaValue);
        } catch (Exception e) {
            throw new TypeLoadingException("Theme resource " + name +
                                           " is not a valid alpha composite. " +
                                           "The composite " + value + " does not exist.");
        }
    }

}
