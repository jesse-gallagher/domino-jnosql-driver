# Domino Diana Driver

**Domino**: [Domino](https://www.ibm.com/us-en/marketplace/ibm-domino) is the server counterpart to IBM Notes and is an ancestor document-type NoSQL database.

**JNoSQL Diana**: See [https://github.com/eclipse/jnosql-diana-driver](https://github.com/eclipse/jnosql-diana-driver)

### How To Test

The driver assumes that you are running within an initialized Notes/Domino environment, such as Domino itself or an application that has run `NotesInitExtended` via one mechanism or another.

### How To Compile

Because Notes.jar is not readily available in a Maven-consumable form, this project requires that you specify a Maven property of `notes-jar` to point to the file in your local system.