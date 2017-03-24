/* Copyright (C) Laboratory of Medical Informatics,
 * Aristotle University of Thessaloniki
 * All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Nikolaos Beredimas <beredim@auth.gr>, 2014 - 2016
 */
package gr.auth.med.lomi.beredim.util.jerseyturtle;

import javax.ws.rs.core.MediaType;
import org.apache.jena.riot.Lang;

/**
 *
 * ExtendedMediaType extends javax.ws.rs.core.MediaType to include text/turtle
 * MIME type.
 * @author beredim
 */
public class ExtendedMediaType extends MediaType{
    
    public static final String TEXT_TURTLE_STRING = "text/turtle";
    public static final MediaType TEXT_TURTLE_MEDIATYPE = new MediaType("text", "turtle");
    public static final String TURTLE_STRING = "TURTLE";
    public static final Lang TURTLE_LANG = Lang.TURTLE;
    
}
