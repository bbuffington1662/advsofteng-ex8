package ex09;

import java.util.Scanner;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;

import javassist.CannotCompileException;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.NotFoundException;
import javassist.expr.ExprEditor;
import javassist.expr.NewExpr;


public class BuffingtonLoader extends ClassLoader {

	static final String WORK_DIR = System.getProperty("user.dir");
	   static final String INPUT_PATH = WORK_DIR + File.separator + "classfiles";
	   static String TARGET_MY_APP = null;
	   static int num = 0;
	   static String _L_ = System.lineSeparator();

	   public static void main(String[] args) throws Throwable {
		   Scanner input = new Scanner(System.in);
		   String[] values = null;
		   
		   System.out.print("Please enter the classname and parameter count separated by commas: ");
			values = input.nextLine().split(",");
			
			while (values.length != 2) {
				System.out.println("[WRN] Invalid input size!!");
				System.out.print("Please enter the classname and parameter count separated by commas: ");
				values = input.nextLine().split(",");
			}
			TARGET_MY_APP  = values[0];
			num = Integer.parseInt(values[1]);
		   
		  BuffingtonLoader s = new BuffingtonLoader();
		  TARGET_MY_APP  = values[0];
		  num = Integer.parseInt(values[1]);
	      Class<?> c = s.loadClass(TARGET_MY_APP);
	      Method mainMethod = c.getDeclaredMethod("main", new Class[] { String[].class });
	      mainMethod.invoke(null, new Object[] { args });
	   }

	   private ClassPool pool;

	   public BuffingtonLoader() throws NotFoundException {
	      pool = new ClassPool();
	      pool.insertClassPath(new ClassClassPath(new java.lang.Object().getClass()));
	      pool.insertClassPath(INPUT_PATH); // TARGET must be there.
	   }

	   /*
	    * Finds a specified class. The bytecode for that class can be modified.
	    */
	   protected Class<?> findClass(String name) throws ClassNotFoundException {
		      CtClass cc = null;
		      try {
		         cc = pool.get(name);
		         cc.instrument(new ExprEditor() {
		            public void edit(NewExpr newExpr) throws CannotCompileException {
		               try {
		                  String longName = newExpr.getConstructor().getLongName();
		                  if (longName.startsWith("java.")) {
		                     return;
		                  }
		               } catch (NotFoundException e) {
		                  e.printStackTrace();
		               }
		               String log = String.format("[Edited by ClassLoader] new expr: %s, " //
		                     + "line: %d, signature: %s", newExpr.getEnclosingClass().getName(), //
		                     newExpr.getLineNumber(), newExpr.getSignature());
		               System.out.println(log);
		               
		               CtField fields[] = newExpr.getEnclosingClass().getDeclaredFields();
		               
		               String fieldName0 = fields[0].getName();
		               String block1 = "";
		               try {
					
						String fieldType0 = fields[0].getType().getName();

		               block1 = String.format("{\n\t$_ = $proceed($$);\n\t{\n\t\tString cName = $_.getClass().getName();\n\t\tString fname = $_.getClass().getDeclaredFields()[%d].getName();\n\t\tString fieldFullName = cName + \".\" + fname;\n\t\t%s fieldValue%d = $_.%s;\n\t\tSystem.out.println(\"\t[Instrument] \" + fieldFullName + \": \" + fieldValue%d);",  0, fieldType0, 0, fieldName0, 0);
		               
					} catch (NotFoundException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
		               
		               int count = 0;
		               
		               while (++count < num && count < fields.length) {
		            	   try {   
		            		   String fieldName = fields[count].getName();
		            		   String fieldType = fields[count].getType().getName();
		            	   
		            	   block1 = String.format("%s\n\t\tcName = $_.getClass().getName();\n\t\tfname = $_.getClass().getDeclaredFields()[%d].getName();\n\t\tfieldFullName = cName + \".\" + fname;\n\t\t%s fieldValue%d = $_.%s;\n\t\tSystem.out.println(\"\t[Instrument] \" + fieldFullName + \": \" + fieldValue%d);", block1, count, fieldType, count, fieldName, count);
		            	   } catch (NotFoundException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
		            	   
		               }
		               block1 = String.format("%s\n\t}\n}", block1);
		               System.out.println(block1);
		               newExpr.replace(block1);
		            }
		         });
		         byte[] b = cc.toBytecode();
		         return defineClass(name, b, 0, b.length);
		      } catch (NotFoundException e) {
		         throw new ClassNotFoundException();
		      } catch (IOException e) {
		         throw new ClassNotFoundException();
		      } catch (CannotCompileException e) {
		         e.printStackTrace();
		         throw new ClassNotFoundException();
		      }
		   }
	}
