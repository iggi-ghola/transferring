package com.kozhekin.transferring;

import com.kozhekin.transferring.dao.ApplicationDao;
import com.kozhekin.transferring.model.Account;
import com.kozhekin.transferring.model.MoneyTransaction;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Main rest interface
 */
@ApplicationPath("/")
@Produces("application/json")
@Path("/")
public class Application extends javax.ws.rs.core.Application {

    // It seems ugly but I cannot find a simple way, how to enforce Undertow
    // to initialize Application class with correct instance of applicationDao
    private static ApplicationDao applicationDao;

    static void setApplicationDao(ApplicationDao applicationDao) {
        Application.applicationDao = applicationDao;
    }

    @PUT
    @Path("/account/create")
    public Response createAccount(@FormParam("email") String email) {
        try {
            final Account account = applicationDao.createAccount(email);
            return Response.status(Response.Status.CREATED).entity(account).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(buildEntity("success", Boolean.FALSE, "message", e.getMessage())).build();
        }
    }

    @GET
    @Path("/account/list")
    public Response getAccounts() {
        return Response.ok(applicationDao.getAllAccounts()).build();
    }

    @GET
    @Path("/account/{accountId}")
    public Response getAccount(@PathParam("accountId") int accountId) {
        return Response.ok(applicationDao.getAccount(accountId)).build();
    }

    @POST
    @Path("/account/deposit")
    public Response deposit(@FormParam("dstId") int dstId, @FormParam("amount") BigDecimal amount) {
        try {
            final MoneyTransaction t = applicationDao.deposit(dstId, amount);
            return Response.ok(t).build();
        } catch (Exception e) {
            return errorStatus(e.getMessage());
        }
    }

    @POST
    @Path("/account/transfer")
    public Response transfer(@FormParam("srcId") int srcId, @FormParam("dstId") int dstId, @FormParam("amount") BigDecimal amount) {
        try {
            final MoneyTransaction t = applicationDao.transfer(srcId, dstId, amount);
            return Response.ok(t).build();
        } catch (Exception e) {
            return errorStatus(e.getMessage());
        }
    }

    @GET
    @Path("/transaction/list")
    public Response getTransactions() {
        return Response.ok(applicationDao.getTransactions()).build();
    }

    @GET
    @Path("/transaction/{accountId}")
    public Response getTransaction(@PathParam("accountId") int accountId) {
        return Response.ok(applicationDao.getTransactions(accountId)).build();
    }


    private Response errorStatus(final String message) {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(buildEntity("success", Boolean.FALSE, "message", message)).build();
    }

    /**
     * Build a Map from parameters
     *
     * @param os must have even amount of elements [key1,value1,key2,value2...]
     * @return Map
     */
    private Map<String, ?> buildEntity(Object... os) {
        final Map<String, Object> m = new HashMap<>();
        for (int i = 0; i < os.length; i += 2) {
            m.put((String) os[i], os[i + 1]);
        }
        return m;
    }

    @Override
    public Set<Class<?>> getClasses() {
        return Collections.singleton(Application.class);
    }
}
