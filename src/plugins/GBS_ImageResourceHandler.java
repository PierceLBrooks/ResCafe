/* $Header: /home/gbsmith/projects/MacResReader/ResCafe_1.0/src/plugins/RCS/GBS_ImageResourceHandler.java,v 1.2 1999/10/21 21:21:16 gbsmith Exp $ */

import com.sun.jimi.core.Jimi; // JIMI - tools for image I/O

import java.awt.BorderLayout;
import java.awt.Image;

import java.io.File;

import ResourceManager.*;

/*=======================================================================*/
/*
 * $Log: GBS_ImageResourceHandler.java,v $
 * Revision 1.2  1999/10/21 21:21:16  gbsmith
 * Added copyright notice. Made class imports more explicit.
 *
 * Revision 1.1  1999/10/04 21:58:15  gbsmith
 * Initial revision
 *
 */

/*=======================================================================*/
/* Copyright (c) 1999 by G. Brannon Smith -- All Rights Reserved         */
/*=======================================================================*/

/*=======================================================================*/
public abstract class GBS_ImageResourceHandler extends MacResourceHandler
{
   /*--- Data -----------------------------------------------------------*/
   protected Image myimages[];

    /*------ RCS ---------------------------------------------------------*/
   static final String rcsid = "$Id: GBS_ImageResourceHandler.java,v 1.2 1999/10/21 21:21:16 gbsmith Exp $";

   /*--- Methods --------------------------------------------------------*/
   public void save ( File savedir )
   {
      StringBuffer tmpfilename;
      String filename;
      String saveType;

      if(resData == null)
      {
         System.err.println("ERROR: No resources to save");
         return;
      }

      Resource myResArray[] = resData.getResArray();
      saveType = resData.getID();

      System.out.println("Saving resources of type \'" + saveType + "\'");
      for(int i=0; i < myResArray.length; i++)
      {
         tmpfilename = new StringBuffer(savedir.getPath());
         tmpfilename.append( File.separator + myResArray[i].getID() );
         if(myResArray[i].getName() != null)
            tmpfilename.append("_" + myResArray[i].getName());
         tmpfilename.append(".xpm");
         filename = tmpfilename.toString().replace(' ', '_');
         //System.out.println("\tSaving \'" + filename + "\'...");

         try
         {
            Jimi.putImage(myimages[i], filename);
         } catch (Exception whatever) {
            System.err.println("ERROR: Got exception " + whatever );
         }
      }
   }
}
