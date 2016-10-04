Task
Implement client and server in Java.

Description

Server offers service for player wallet (balance). Wallet state (balance) should be managed in memory (3rd party solution may not be used). Balance is backed up in database (hsql). When balance is not in memory, it is loaded from database and any changes are done in memory.
Player record in database is created on demand. There is a periodical background process to write changes from memory to database.

Constraints on balance changes:

1.       Balance cannot be less than 0.

2.       If transaction exists (is duplicate), then previous response is returned. Check may take into consideration only 1000 latest transactions.

3.       If balance change is bigger than configured limit, then change is denied (explained further below).

4.       If player is in blacklist, then change is denied (explained further below).

Configuration (balance change limit and player blacklist) must be taken from external source. This can be file, database, external component etc.

Client itself is a server that offers gameplay logic. Specific gameplay will not be implemented, client can just generate random balance updates. Specific communication protocol between client and server is not specified (custom protocol can be invented). Server must write proper log information, where at least IN/OUT per player must be grep’able.

Commands between servers:

client->server: username, transaction id, balance change

server->client: transaction id, error code, balance version, balance change, balance after change

Database structure

PLAYER(USERNAME, BALANCE_VERSION, BALANCE)

Documentation:

·          Describe shortly the implementation aspects.

·          If some features are not implemented, point out the reasons.
=================================================================================================
   Solution.

1. Transport implemented as JSON messages over TCP/IP. JSON chosen to make the solution more transparent.
   That allows to communicate with the server in text mode (with telnet, for example), "\n" treated as
   end-of-message marker.

2. Server implemented with Java NIO Selectors API. This allows to handle arbitrary number of connections
    with limited number of threads, also provides best performance (ByteBuffers and nio channels play).

3. Components of the system are decoupled as possible and separated with well-defined interfaces.
   This allows more flexibility , extensibility, etc.

4. Components are bound together with dependency injection technique (Spring contexts). This allows
   change composition with just running upon different context definition.

5. The most interesting part is solving concurrency. JSR166 ConcurrentLinkedHashMap would significantly
   simplify solution, but since 3rd party components are not allowed, we provided solution composed from
   JDK components only. The solution seems to be concurrency holes free. (We believe).
   In general, we tried to achieve maximum efficiency. Almost no locks are used, all concurrency
   claimed to be solved on CAS operations and ordering guarantees.

6. Unit tests show and explain basic expectations.

7. HSQL used in embedded mode to provide zero deployment. Can easily be extended to full-scale DB.

8. Build/run tests with Maven: "mvn clean test package" in root of the project.

9. What could be improved further (TODO list):
   - Better tests for DbSaver component.
   - Add batch update MERGE syntax for HSQL, if possible.
   - More configurability (server addresses, buffer sizes, etc.)
