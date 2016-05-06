package com.kozhekin;

import com.kozhekin.dao.Dao;
import com.kozhekin.model.Account;
import com.kozhekin.model.Transaction;
import com.kozhekin.model.TransactionState;
import com.kozhekin.model.User;
import com.kozhekin.service.TransferringService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);
    private final Dao dao;
    private final TransferringService service;

    public Application() {
        dao = new Dao();
        service = new TransferringService();
    }

    @POST
    @Path("/user/create")
    public Response createUser(@FormParam("email") String email,
                               @FormParam("phone") String phone) {
        try {
            // TODO put reasonable validation here
            final User u = dao.createUser(email, phone);
            return Response.ok(u).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(buildEntity("success", Boolean.FALSE, "message", e.getMessage())).build();
        }
    }

    @GET
    @Path("/user/list")
    public Response getUsers() {
        return Response.ok(dao.getUsers()).build();
    }

    @GET
    @Path("/user/{userId}")
    public Response getUsers(@PathParam("userId") int userId) {
        return Response.ok(dao.getUsers(userId)).build();
    }

    @POST
    @Path("/account/create")
    public Response createAccount(@FormParam("userId") int userId, @FormParam("type") String type) {
        try {
            final Account a = dao.createAccount(userId, type);
            return Response.ok(a).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(buildEntity("success", Boolean.FALSE, "message", e.getMessage())).build();
        }
    }

    @POST
    @Path("/account/deposit")
    public Response deposit(@FormParam("dstId") long dstId, @FormParam("amount") BigDecimal amount) {
        try {
            final Transaction t = service.deposit(dstId, amount);
            if (TransactionState.SUCCESS == t.getState()) {
                return Response.ok(t).build();
            } else {
                return errorStatus(t.getComment());
            }
        } catch (Exception e) {
            return errorStatus(e.getMessage());
        }
    }

    @POST
    @Path("/account/transfer")
    public Response transfer(@FormParam("srcId") long srcId, @FormParam("dstId") long dstId, @FormParam("amount") BigDecimal amount) {
        try {
            final Transaction t = service.transfer(srcId, dstId, amount);
            if (TransactionState.SUCCESS == t.getState()) {
                return Response.ok(t).build();
            } else {
                return errorStatus(t.getComment());
            }
        } catch (Exception e) {
            return errorStatus(e.getMessage());
        }
    }

    @GET
    @Path("/transaction/list")
    public Response getTransactions() {
        return Response.ok(dao.getTransactions()).build();
    }

    @GET
    @Path("/transaction/{accountId}")
    public Response getTransactions(@PathParam("accountId") long accountId) {
        return Response.ok(dao.getTransactions(accountId)).build();
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
