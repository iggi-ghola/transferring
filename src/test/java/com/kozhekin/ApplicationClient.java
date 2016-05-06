package com.kozhekin;

import com.kozhekin.model.Account;
import com.kozhekin.model.Transaction;
import com.kozhekin.model.User;

import javax.ws.rs.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * Rest client for test purposes
 */
@Produces("application/json")
public interface ApplicationClient {
    @POST
    @Path("/user/create")
    User createUser(@FormParam("email") String email,
                    @FormParam("phone") String phone);

    @GET
    @Path("/user/list")
    List<User> getUsers();

    @GET
    @Path("/user/{userId}")
    List<User> getUsers(@PathParam("userId") int userId);

    @POST
    @Path("/account/create")
    Account createAccount(@FormParam("userId") int userId, @FormParam("type") String type);

    @POST
    @Path("/account/deposit")
    Transaction deposit(@FormParam("dstId") long dstId, @FormParam("amount") BigDecimal amount);

    @POST
    @Path("/account/transfer")
    Transaction transfer(@FormParam("srcId") long srcId, @FormParam("dstId") long dstId, @FormParam("amount") BigDecimal amount);

    @GET
    @Path("/transaction/list")
    List<Transaction> getTransactions();

    @GET
    @Path("/transaction/{accountId}")
    List<Transaction> getTransactions(@PathParam("accountId") long accountId);
}
