package com.mycompany.app;

import static spark.Spark.get;
import static spark.Spark.port;
import static spark.Spark.post;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import spark.ModelAndView;
import spark.template.mustache.MustacheTemplateEngine;
import java.io.File;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


public class App {
	static ArrayList<String> parseList = new ArrayList<>();
	
	static class UserHandler extends DefaultHandler {
		   String name="";
		   String surname="";
		   String id="";
		   String res="";
		   boolean readFirstName = false;
		   boolean readLastName = false;
		   boolean readName = false;
		   

		   @Override
		   public void startElement(
		      String uri, String localName, String qName, Attributes attributes)
		      throws SAXException {
		      
		      if (qName.equalsIgnoreCase("NAME")) {
		         id = attributes.getValue("Id");
		         res+=id+" ";
		         System.out.println("Id : " + id);
		      } else if (qName.equalsIgnoreCase("FIRSTNAME")) {
		    	  		readFirstName = true;
		      } else if (qName.equalsIgnoreCase("LASTNAME")) {
		    	  		readLastName = true;
		      }
		   }   

		   @Override
		   public void characters(char ch[], int start, int length) throws SAXException {

		      if (readFirstName) {
		         res+=name+" ";
		    	 System.out.println("First Name: " + new String(ch, start, length));
		         readFirstName = false;
		      } else if (readLastName) {
		         res+=surname;
		         parseList.add(res);
		         res="";
		    	 System.out.println("Last Name: " + new String(ch, start, length));
		         readLastName = false;
		      }
		   }
		}
   

    public static void main(String[] args) {
    		      
    	
        port(getHerokuAssignedPort());

        get("/", (req, res) -> "Hello, World");

        post("/compute", (req, res) -> {
            // System.out.println(req.queryParams("input1"));
            // System.out.println(req.queryParams("input2"));
        	
        	 try {
		         File inputFile = new File("EEAS.xml");
		         SAXParserFactory factory = SAXParserFactory.newInstance();
		         SAXParser saxParser = factory.newSAXParser();
		         UserHandler userhandler = new UserHandler();
		         saxParser.parse(inputFile, userhandler);     
		      } catch (Exception e) {
		    	  System.out.println("FAILED");
		         e.printStackTrace();
		      }
        	
        	

        	String input1 = req.queryParams("input1").replaceAll("\\s", "");
            int input1AsInt = Integer.parseInt(input1);

            String input2 = req.queryParams("input2").replaceAll("\\s", "");
            int input2AsInt = Integer.parseInt(input2);
            
            String person="";
            String line="";
            String search="";
            if(!input1.equals(" ") && input2.equals(" ")) {
            	for(int i=0;i<parseList.size();i++) {
            			line=parseList.get(i);
            			line=line.substring(line.indexOf(" ")+1);
            			line=line.substring(0,line.indexOf(" "));
            			
            		if(input1.equals(line)) {
            			search+=parseList.get(i)+" ";
            		}
            	}
            }else if(input1.equals(" ") && !input2.equals(" ")) {
            	for(int i=0;i<parseList.size();i++) {
        			line=parseList.get(i);
        			line=line.substring(line.indexOf(" ")+1);
        			line=line.substring(line.indexOf(" ")+1);
        			
        		if(input2.equals(line)) {
        			search+=parseList.get(i)+" ";
        		}
            	}
            }else if(!input1.equals(" ") && !input2.equals(" ")) {
            	for(int i=0;i<parseList.size();i++) {
        			line=parseList.get(i);
        			line=line.substring(line.indexOf(" ")+1);
        			
        		if((input1+" "+input2).equals(line)) {
        			search+=parseList.get(i)+" ";
        		 }
            	}
            }

            

            Map map = new HashMap();
            map.put("result", search);
            return new ModelAndView(map, "compute.mustache");
        }, new MustacheTemplateEngine());

        get("/compute", (rq, rs) -> {
            Map map = new HashMap();
            map.put("result", "not computed yet!");
            return new ModelAndView(map, "compute.mustache");
        }, new MustacheTemplateEngine());
    }

    static int getHerokuAssignedPort() {
        ProcessBuilder processBuilder = new ProcessBuilder();
        if (processBuilder.environment().get("PORT") != null) {
            return Integer.parseInt(processBuilder.environment().get("PORT"));
        }
        return 4567; // return default port if heroku-port isn't set (i.e. on localhost)
    }
}







