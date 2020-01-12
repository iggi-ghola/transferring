package com.kozhekin.transferring;


import com.kozhekin.transferring.modelTest.Account;
import com.kozhekin.transferring.modelTest.MoneyTransaction;

import javax.ws.rs.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * Rest client for test purposes
 */
@Produces("application/json")
public interface ApplicationClient {
    @PUT
    @Path("/account/create")
    Account createAccount(@FormParam("email") String email);

    @GET
    @Path("/account/list")
    List<Account> getAccounts();

    @GET
    @Path("/account/{accountId}")
    Account getAccount(@PathParam("accountId") int accountId);

    @POST
    @Path("/account/deposit")
    MoneyTransaction deposit(@FormParam("dstId") int dstId, @FormParam("amount") BigDecimal amount);

    @POST
    @Path("/account/transfer")
    MoneyTransaction transfer(@FormParam("srcId") int srcId, @FormParam("dstId") int dstId, @FormParam("amount") BigDecimal amount);

    @GET
    @Path("/transaction/list")
    List<MoneyTransaction> getTransactions();

    @GET
    @Path("/transaction/{accountId}")
    List<MoneyTransaction> getTransactions(@PathParam("accountId") long accountId);
}
