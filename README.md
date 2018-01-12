# Domino Diana Driver

**Domino**: [Domino](https://www.ibm.com/us-en/marketplace/ibm-domino) is the server counterpart to IBM Notes and is an ancestor document-type NoSQL database.

**JNoSQL Diana**: See [https://github.com/eclipse/jnosql-diana-driver](https://github.com/eclipse/jnosql-diana-driver)

## Warning

This driver is currently extraordinarily inefficient. All querying and selection is done via `database.search`, with no optimization. Some of this may be improved by checking for queries that only do a key/UNID lookup and use that, which would help in a lot of normal cases.

For real querying performance, the problem is greater. It could be possible to use FT indexes for normal searches, but they're often unreliable. Better would be to use an alternative indexing mechanism entirely.

Additionally, a graph implementation using ODA's Tinkerpop hook would perform very well.

### How To Test

The driver assumes that you are running within an initialized Notes/Domino environment, such as Domino itself or an application that has run `NotesInitExtended` via one mechanism or another. The tests initialize the environment and currently are only known to run on Windows.

### How To Compile

Because Notes.jar is not readily available in a Maven-consumable form, this project requires that you specify a Maven property of `notes-jar` to point to the file in your local system.