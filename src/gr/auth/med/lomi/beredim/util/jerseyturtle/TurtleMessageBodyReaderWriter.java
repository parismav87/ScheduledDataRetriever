/* Copyright (C) Laboratory of Medical Informatics,
 * Aristotle University of Thessaloniki
 * All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Nikolaos Beredimas <beredim@auth.gr>, 2014 - 2016
 */
package gr.auth.med.lomi.beredim.util.jerseyturtle;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.impl.ModelCom;
import org.apache.jena.riot.RDFDataMgr;

/**
 *
 * TurtleMessageBodyReaderWriter is a JAX-RS Entity Provider that adds support
 * for Jena RDF Models, serialized in turtle syntax. 
 * 
 * @author beredim
 */
@Provider
@Consumes(ExtendedMediaType.TEXT_TURTLE_STRING)
@Produces(ExtendedMediaType.TEXT_TURTLE_STRING)
public class TurtleMessageBodyReaderWriter implements MessageBodyReader<Model>, MessageBodyWriter<Model> {

    //READER IMPLEMENTATION

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return type == Model.class;
    }

    @Override
    public Model readFrom(Class<Model> type, Type genericType, Annotation[] annotations,
            MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
            throws IOException, WebApplicationException {
        Model model = ModelFactory.createDefaultModel();
        RDFDataMgr.read(model, entityStream, ExtendedMediaType.TURTLE_LANG);
        return model;
    }

    //WRITER IMPLEMENTATION
    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations,
            MediaType mediaType) {
        return type == ModelCom.class;
    }

    @Override
    public long getSize(Model model, Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType) {
        // deprecated by JAX-RS 2.0 and ignored by Jersey runtime
        return 0;
    }

    @Override
    public void writeTo(Model model, Class<?> type, Type genericType, Annotation[] annotations,
            MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
            throws IOException, WebApplicationException {
        model.write(entityStream, ExtendedMediaType.TURTLE_STRING);
    }

}
