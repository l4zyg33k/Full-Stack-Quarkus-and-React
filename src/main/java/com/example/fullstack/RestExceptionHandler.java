package com.example.fullstack;

import io.vertx.pgclient.PgException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.hibernate.HibernateException;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.StaleObjectStateException;

import java.util.Objects;
import java.util.Optional;

@Provider
public class RestExceptionHandler implements ExceptionMapper<HibernateException> {
    private static final String PG_UNIQUE_VIOLATION_ERROR = "23505";

    private static boolean hasExceptionInChain(Throwable throwable, Class<? extends Throwable> exceptionClass) {
        return getExceptionInChain(throwable, exceptionClass).isPresent();
    }

    private static <T extends Throwable> Optional<T> getExceptionInChain(Throwable throwable, Class<T> exceptionClass) {
        while (throwable != null) {
            if (exceptionClass.isInstance(throwable)) {
                return Optional.of((T) throwable);
            }
            throwable = throwable.getCause();
        }
        return Optional.empty();
    }

    @Override
    public Response toResponse(HibernateException e) {
        if (hasExceptionInChain(e, ObjectNotFoundException.class)) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
        if (hasExceptionInChain(e, StaleObjectStateException.class) || hasPostgresErrorCode(e, PG_UNIQUE_VIOLATION_ERROR)) {
            return Response.status(Response.Status.CONFLICT).build();
        }
        return Response.status(Response.Status.BAD_REQUEST).entity("\"" + e.getMessage() + "\"").build();
    }

    private boolean hasPostgresErrorCode(Throwable throwable, String sqlState) {
        return getExceptionInChain(throwable, PgException.class).filter(ex -> Objects.equals(ex.getSqlState(), sqlState)).isPresent();
    }
}
