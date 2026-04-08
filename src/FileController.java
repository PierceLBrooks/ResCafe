/* $Header: /home/gbsmith/projects/MacResReader/ResCafe_1.0/src/RCS/FileController.java,v 1.6 1999/10/21 22:38:58 gbsmith Exp $ */

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import java.awt.Frame;

import java.io.File;
import java.io.PrintWriter;
import java.io.RandomAccessFile;

import java.util.Enumeration;

import ResourceManager.*;

/*====================================================================*/
/*
 * $Log: FileController.java,v $
 * Revision 1.6  1999/10/21 22:38:58  gbsmith
 * Added Copyright notice. Added a few comments.
 *
 * Revision 1.5  1999/10/17 20:37:05  gbsmith
 * Added error handling stuff for invalid regular files:
 *   - Display a dialog upon failure
 *   - Do loading with a temp ResourceModel so as to avoid
 *     overwriting existing model data if load fails
 *
 * Revision 1.4  1999/10/13 07:10:32  gbsmith
 * Added MacBinaryHeader check
 *
 * Revision 1.3  1999/10/08 03:16:58  gbsmith
 * Separated User-interaction functionality (i.e. GUI stuff) into FilePicker
 * and subclasses - this makes the Controller more independent and allows for
 * possible redesign or reuse.
 *
 * Revision 1.2  1999/10/04 21:10:40  gbsmith
 * Made class imports more explicit. Added Resource save functionality.
 *
 * Revision 1.1  1999/09/30 05:19:34  gbsmith
 * Initial revision
 */

/*=======================================================================*/
/* Copyright (c) 1999 by G. Brannon Smith -- All Rights Reserved         */
/*=======================================================================*/

/*====================================================================*/
class FileController implements ActionListener
{
   /*--- Data --------------------------------------------------------*/
   ResourceModel resmod;
   HandlerTable  htab;
   String currentType; // Should be set by the view
   Frame mrview; // Does this even need to be here? Should it be Object?

   FilePicker myfp;

   File tmpFile;
   RandomAccessFile tmpRAFile;
   MacBinaryHeader mbh;

   PrintWriter texelWriter;

   /*------ RCS ---------------------------------------------------------*/
   static final String rcsid = "$Id: FileController.java,v 1.6 1999/10/21 22:38:58 gbsmith Exp $";

   /*--- Methods -----------------------------------------------------*/
   public FileController()
   {
      resmod = null;
      mrview = null;
      currentType = null;
      myfp = new JFilePicker();
      //myfp = new AWTFilePicker();
   }

   /*-----------------------------------------------------------------*/
   public void setResModel( ResourceModel inmod )
   {
      resmod = inmod;
      currentType = null; // Reset this
   }

   /*-----------------------------------------------------------------*/
   public void setHandlers( HandlerTable inHtab )
   {
      htab = inHtab;
   }

   /*-----------------------------------------------------------------*/
   public void setView(Frame inview)
   {
      mrview = inview;
      currentType = null; // Reset this
   }

   /*-----------------------------------------------------------------*/
   public void setCurrentType( String newtype )
   {
      currentType = newtype;
   }

   /*-----------------------------------------------------------------*/
   public void actionPerformed( ActionEvent ae )
   {
      String command = ae.getActionCommand();
      if(resmod == null)
         System.err.println("ERROR: no ResourceModel available\n");
      else
      {
         if(command.equals("Open..."))           doOpenFile( );
         else if(command.equals("Save All"))     doSaveAll( );
         else if(command.equals("Save Handled")) doSaveHandled( );
         else if(command.equals("Save Current")) doSaveCurrent( );
      }
   }

   /*--------------------------------------------------------------------*/
   void doSaveAll()
   {
      File typedir, dirToSave;
      String mytype;
      Enumeration typeKeys;
      MacResourceHandler saveHandler = null;

      /*-----------------------------------------------------------------*/
      dirToSave =  myfp.getSaveDir("Save All Types",
                                   resmod.getFilename() + "_export");
      if( dirToSave != null )
      {
         // Start saving
         typeKeys = resmod.getTypes();
         while( typeKeys.hasMoreElements() )
         {
            mytype = (String)typeKeys.nextElement();

            if(htab.canHandleType(mytype))
               try
               {
                  saveHandler =
                     (MacResourceHandler)htab.getHandler(mytype).newInstance();
               } catch (Exception e) {
                  System.err.println(e);
               }
            else
               saveHandler = new DefaultResourceHandler();


            typedir = new File(dirToSave, mytype);
            if(typedir.exists() && !typedir.isDirectory()) typedir.delete();
            if(!typedir.exists()) typedir.mkdir();

            saveHandler.setResData(resmod.getResourceType(mytype));
            saveHandler.setResModel(resmod);
            saveHandler.init();
            saveHandler.save( typedir );
         }
      }
   }

   /*--------------------------------------------------------------------*/
   void doSaveHandled()
   {
      File dirToSave, typedir;
      MacResourceHandler saveHandler = null;
      String mytype;
      Enumeration typeKeys;

      dirToSave = myfp.getSaveDir("Save Handled Types",
                                   resmod.getFilename() + "_export");

      if( dirToSave != null )
      {
         // Start saving
         typeKeys = resmod.getTypes();
         while( typeKeys.hasMoreElements() )
         {
            mytype = (String)typeKeys.nextElement();
            if(htab.canHandleType(mytype))
            {
               try
               {
                  saveHandler = (MacResourceHandler)htab.
                     getHandler(mytype).newInstance();
               } catch (Exception e) {
                  System.err.println(e);
               }

               typedir = new File(dirToSave, mytype);
               if(typedir.exists() && !typedir.isDirectory()) typedir.delete();
               if(!typedir.exists()) typedir.mkdir();

               saveHandler.setResData( resmod.getResourceType(mytype) );
               saveHandler.setResModel( resmod );
               saveHandler.init();
               saveHandler.save( typedir );
            }
         }
      }
   }

   /*--------------------------------------------------------------------*/
   void doSaveCurrent()
   {
      File typedir, dirToSave;
      MacResourceHandler saveHandler = null;

      if(currentType == null)
      {
         System.err.println("ERROR: no Type Selected");
         return;
      }

      // Be sure there is something to save
      if(resmod == null)
      {
         System.err.println("ERROR: no Resource Model Available");
         return;
      }

      dirToSave = myfp.getSaveDir("Save Current Type",
                                  resmod.getFilename() + "_export");
      if( dirToSave != null )
      {
         // Start saving
         if(htab.canHandleType(currentType))
         {
            try
            {
               saveHandler =
                  (MacResourceHandler)htab.getHandler(currentType).newInstance();
            } catch (Exception e) {
               System.err.println(e);
            }
         } else {
            saveHandler = new DefaultResourceHandler();
         }

         typedir = new File(dirToSave, currentType);
         if(typedir.exists() && !typedir.isDirectory()) typedir.delete();
         if(!typedir.exists()) typedir.mkdir();

         saveHandler.setResData(resmod.getResourceType(currentType));
         saveHandler.setResModel( resmod);
         saveHandler.init();
         saveHandler.save( typedir );
      }
   }

   /*-----------------------------------------------------------------*/
   void doOpenFile()
   {
      File fileToOpen = myfp.getFileToOpen();
      if(fileToOpen != null) loadFile( fileToOpen );
   }


   /*-----------------------------------------------------------------*/
   void loadFile( File inFile )
   {

      if(!inFile.exists())
      {
         myfp.tellFileMissing( inFile.getName() );
         return;
      }

      if(!inFile.isFile())
      {
         myfp.tellNotFile( inFile.getName() );
         return;
      }

      // File exists - open and load
      try
      {
         /*
           This weirdness with the temporary model and the 'become'
           method allows us to attempt to load new Resource data
           and only commit it if we know it succeeded.
           
           Doing an 'init' on the existing model could wipe out the
           existing valid data upon failure.
           
           Simply assigning a new instance to resmod would only apply
           to this class - other classes would still reference
           the old model instance and resource data it contains.

           Of course this is all based on the assumption that we would
           rather have the old data there rather than nothing.
         */
         ResourceModel tmpResMod = new ResourceModel();
         
         tmpRAFile = new RandomAccessFile(inFile, "r");

         tmpResMod.init();
         tmpResMod.setFilename(inFile.getPath());

         // Check to see if this is a MacBinary file
         mbh = new MacBinaryHeader();
         mbh.read(tmpRAFile);         
         if(mbh.validate()) 
         {
            // This is a MacBinary file - must always seek to ResFork
            tmpRAFile.seek(mbh.getResForkOffset());
            tmpResMod.read(tmpRAFile, mbh.getResForkOffset());
         } else {
            // ASSUME an extracted Resource Fork - must always seek to top
            tmpRAFile.seek(0);
            tmpResMod.read(tmpRAFile);
         }

         tmpRAFile.close();
         resmod.become(tmpResMod);
      } catch(Exception ioe) {
         myfp.tellCannotOpen( inFile.getName() );
      }

      tmpRAFile = null;
   }
}
