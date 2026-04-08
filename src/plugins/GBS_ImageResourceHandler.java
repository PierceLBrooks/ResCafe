/* $Header: /home/gbsmith/projects/ResCafe/ResCafe1.2.5/src/plugins/RCS/GBS_ImageResourceHandler.java,v 1.4 2000/05/24 06:21:14 gbsmith Exp $ */

import java.awt.BorderLayout;
import java.awt.Image;

import java.awt.image.IndexColorModel;
import java.awt.image.MemoryImageSource;

import java.io.File;
import java.io.FileWriter;

import ResourceManager.*;

/*=======================================================================*/
/* Copyright (c) 1999-2000 by G. Brannon Smith -- All Rights Reserved    */
/*=======================================================================*/

/*=======================================================================*/
/*
 * $Log: GBS_ImageResourceHandler.java,v $
 * Revision 1.4  2000/05/24 06:21:14  gbsmith
 * Now uses custom XpmImage class instead of Jimi for XPM export.
 * Also now subclasses DefaultResourceHandler rather that
 * MacResourceHandler. This allow access to column sorting and
 * optimizing code. Still descends from MacResourceHandler though.
 *
 * Revision 1.3  1999/12/19 05:17:37  gbsmith
 * Moved icon processing/decoding routines into this class from
 * IconFamilyResourceHandler.
 *
 * Revision 1.2  1999/10/21 21:21:16  gbsmith
 * Added copyright notice. Made class imports more explicit.
 *
 * Revision 1.1  1999/10/04 21:58:15  gbsmith
 * Initial revision
 *
 */

/*=======================================================================*/
public abstract class GBS_ImageResourceHandler extends DefaultResourceHandler
{
   /*--- Data -----------------------------------------------------------*/
   protected Image myimages[];

   // Make the color models
   protected IndexColorModel icm16  = MacStandard16Palette.getColorModel();
   protected IndexColorModel icm256 = MacStandard256Palette.getColorModel();

    /*------ RCS ---------------------------------------------------------*/
   static final String rcsid = "$Id: GBS_ImageResourceHandler.java,v 1.4 2000/05/24 06:21:14 gbsmith Exp $";

   /*--- Methods --------------------------------------------------------*/
   protected Image process_ICN( byte rawData[] )
   {
      MemoryImageSource mis;
      byte iconData[];

      int i, j, b;

      // Grab icon data
      iconData = new byte[1024];
      for ( j = 0; j < 128; j++)
         for ( b = 0; b < 8; b++)
            iconData[j*8+b] = (byte)((rawData[j] &
                                      (0x80 >>> b)) > 0? 0x0F: 0x00);

      mis = new MemoryImageSource(32, 32, icm16, iconData, 0, 32);
      return createImage(mis);
   }

   /*--------------------------------------------------------------------*/
   protected Image process_ICN_mask( byte rawData[] )
   {
      MemoryImageSource mis;
      byte maskData[];
      int i, j, b;

      // Grab mask data
      maskData = new byte[1024];
      for ( j = 0; j < 128; j++)
         for ( b = 0; b < 8; b++)
            maskData[j*8+b] = (byte)((rawData[j + 128] &
                                      (0x80 >>> b)) > 0? 0x0F: 0x00);

      mis = new MemoryImageSource(32, 32, icm16, maskData, 0, 32);
      return createImage(mis);
   }

   /*--------------------------------------------------------------------*/
   protected Image process_icl4( byte rawData[] )
   {
      MemoryImageSource mis;
      byte iconData[];

      iconData = new byte[1024];
      for (int j = 0; j < 512; j++)
      {
         // Grab high 4 bytes
         iconData[j*2]   = (byte)((rawData[j] >> 4) & 0x0F);

         // Grab low 4 bytes
         iconData[j*2+1] = (byte)(rawData[j] & 0x0F);
      }

      mis = new MemoryImageSource(32, 32, icm16, iconData, 0, 32);
      return createImage(mis);
   }

   /*--------------------------------------------------------------------*/
   protected Image process_icl8( byte rawData[] )
   {
      MemoryImageSource mis;

      // Can create icon directly from data
      mis = new MemoryImageSource(32, 32, icm256, rawData, 0, 32);
      return createImage(mis);
   }

   /*--------------------------------------------------------------------*/
   protected Image process_ics( byte rawData[] )
   {
      int i, j, b;
      MemoryImageSource mis;
      byte iconData[];

      // Grab icon data
      iconData = new byte[256];
      for ( j = 0; j < 32; j++)
         for ( b = 0; b < 8; b++)
            iconData[j*8+b] = (byte)((rawData[j] &
                                      (0x80 >>> b)) > 0? 0x0F: 0x00);

      mis = new MemoryImageSource(16, 16, icm16, iconData, 0, 16);
      return createImage(mis);
   }

   /*--------------------------------------------------------------------*/
   protected Image process_ics_mask( byte rawData[] )
   {
      int i, j, b;
      MemoryImageSource mis;
      byte maskData[];

      // Grab mask data
      maskData = new byte[256];
      for ( j = 0; j < 32; j++)
         for ( b = 0; b < 8; b++)
            maskData[j*8+b] = (byte)((rawData[j+32] &
                                      (0x80 >>> b)) > 0? 0x0F: 0x00);

      mis = new MemoryImageSource(16, 16, icm16, maskData, 0, 16);
      return createImage(mis);
   }

   /*--------------------------------------------------------------------*/
   protected Image process_ics4( byte rawData[] )
   {
      MemoryImageSource mis;
      byte iconData[];

      iconData = new byte[256];
      for (int j = 0; j < 128; j++)
      {
         // Grab high 4 bytes
         iconData[j*2]   = (byte)((rawData[j] >> 4) & 0x0F);

         // Grab low 4 bytes
         iconData[j*2+1] = (byte)(rawData[j] & 0x0F);
      }

      mis = new MemoryImageSource(16, 16, icm16, iconData, 0, 16);
      return createImage(mis);
   }

   /*--------------------------------------------------------------------*/
   protected Image process_ics8( byte rawData[] )
   {
      // Can create icon directly from data
      MemoryImageSource mis =
         new MemoryImageSource(16, 16, icm256, rawData, 0, 16);
      return createImage(mis);
   }

   /*--------------------------------------------------------------------*/
   public void save ( File savedir )
   {
      StringBuffer tmpfilename;
      String filename;
      String imgname;
      String saveType;

      XpmImage xpmout;

      File outfile;
      FileWriter fw;

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
         tmpfilename = new StringBuffer( myResArray[i].getID() );
         if(myResArray[i].getName() != null)
         {
            tmpfilename.append("_" + myResArray[i].getName());
            imgname = myResArray[i].getName().replace(' ', '_');
         } else imgname = "untitled";

         tmpfilename.append(".xpm");
         filename = tmpfilename.toString().replace(' ', '_');

         try
         {
            outfile = new File(savedir, filename);
            fw = new FileWriter(outfile);
            xpmout = new XpmImage(imgname, this, myimages[i]);
            xpmout.write(fw);
         } catch (Exception whatever) {
            System.err.println("ERROR: Got exception " + whatever );
         }
      }
   }
}
