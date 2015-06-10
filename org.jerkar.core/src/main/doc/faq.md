# Frequented Asked Questions
----------------------------

## Questions about exceptions launched when Jerkar is running

### Get Provider org.apache.xerces.jaxp.SAXParserFactoryImpl not found. What can i do ?

You probably run a Jerkar on a JDK 6 and use some @JkImport on libraries playing with the XMLParsingServiceFactory.
You can fix either by 
* using a more recent JDK (>7), add a library  
* adding the xerces lib to your JDK endorsed lib folder



