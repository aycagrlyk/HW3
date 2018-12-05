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
	static ArrayList<Person> parseList = new ArrayList<>();
	private static int size = 0;
	public static String firstname="";
	public static String lastname="";
	
	
	static class Person {
		private String fname = "";
		private String lname = "";
		private String entityId = "";

		public Person() {
			fname = "";
			lname = "";
			entityId = "";
		}

		public Person(String f, String l, String n) {
			fname = f;
			lname = l;
			entityId = n;
		}

		public String toString() {
			return this.entityId + "\n" + this.fname + "\n" + this.lname+"\n";
		}

	}

	static class UserHandler extends DefaultHandler {
		private String name = "";
		private String surname = "";
		private String id = "";
		private String res = "";
		private boolean readFirstName = false;
		private boolean readLastName = false;
		private boolean readName = false;
		

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes)
				throws SAXException {

			if (qName.equalsIgnoreCase("ENTITY")) {
				id = attributes.getValue("Id");
				res += id + " ";
			} else if (qName.equalsIgnoreCase("FIRSTNAME")) {
				readFirstName = true;
			} else if (qName.equalsIgnoreCase("LASTNAME")) {
				readLastName = true;
			}
		}

		

		@Override
		public void characters(char ch[], int start, int length) throws SAXException {
			String data=new String(ch, start, length);
			
			
			ArrayList<String> isim =new ArrayList<>();
			

			if (readFirstName==true) {
				firstname = data;
				readFirstName = false;
				
				if(readFirstName==false && readLastName==false){
					parseList.add(size,new Person(firstname,lastname,id));
				}
			
				
			}  
			if (readLastName==true) {
				lastname = data;
				readLastName = false;
				
			}
			
			
		}
		
		
	}
	//Search the given input in arraylist
	public static String search(ArrayList<Person> list,String input1,String input2) {
		String result="";
		ArrayList<Person> find =new ArrayList<>();
	

		if((!input1.equals("")) && input2.equals("")) {
			for(int i=0;i<list.size();i++) {
				if((list.get(i).fname).equalsIgnoreCase(input1)) {
					if(isDefine(find,list.get(i))==false){
						find.add(list.get(i));
						result=list.get(i).toString()+"______________________________\n"+result;
					}
						
				
					
				}
			}
		}else if(input1.equals("") && !input2.equals("")) {
			for(int i=0;i<list.size();i++) {
				if((list.get(i).lname).equalsIgnoreCase(input2)) {
					if(isDefine(find,list.get(i))==false){
						find.add(list.get(i));
						result=list.get(i).toString()+"______________________________\n"+result;
					}
				
					
				}
			}
		}else if(!input1.equals("") && !input2.equals("")) {
			for(int i=0;i<list.size();i++) {
				if((list.get(i).lname).equalsIgnoreCase(input2) && (list.get(i).fname).equalsIgnoreCase(input1)) {
					if(isDefine(find,list.get(i))==false ){
						find.add(list.get(i));
						result=list.get(i).toString()+"______________________________\n"+result;
					}
					
				}
			}
		}
		
		return result;
	}
	//Remove the list that first name and last name are same but id is different ,so same person
	public static boolean isDefine(ArrayList<Person> list,Person p){
		for(int i=0;i<list.size();i++){
			Person other=list.get(i);
			if(p.entityId.equalsIgnoreCase(other.entityId) && p.fname.equalsIgnoreCase(other.fname) && p.lname.equalsIgnoreCase(other.lname)){
				return true;
			}
		}
		return false;
	}

	public static void main(String[] args) {

		port(getHerokuAssignedPort());

		get("/", (req, res) -> "Hello, World");

		post("/search", (req, res) -> {
		

			String input1 = req.queryParams("input1").replaceAll("\\s", "");
		

			String input2 = req.queryParams("input2").replaceAll("\\s", "");
		

			try {
				File inputFile = new File("EEAS.xml");
			
				SAXParserFactory factory = SAXParserFactory.newInstance();
				
				SAXParser saxParser = factory.newSAXParser();
			
				UserHandler userhandler = new UserHandler();
			
				saxParser.parse(inputFile, userhandler);
		
			} catch (Exception e) {
			
				e.printStackTrace();
			}


			String result=search(parseList,input1,input2);
			for(int i=0;i<parseList.size();i++){
				parseList.remove(i);
			}
			Map map = new HashMap();
			//System.out.println(result);
			result=result;
			map.put("result", result);
			result="";
			
			return new ModelAndView(map, "compute.mustache");
		}, new MustacheTemplateEngine());

		get("/search", (rq, rs) -> {
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
