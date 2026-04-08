/* $Header: /home/gbsmith/projects/MacResReader/ResCafe_1.0/src/plugins/RCS/IconFamilyResourceHandler.java,v 1.5 1999/10/19 06:00:46 gbsmith Exp $ */

import com.sun.jimi.core.Jimi; // JIMI - tools for image I/O

import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import java.awt.BorderLayout;
import java.awt.Image;

import java.awt.image.IndexColorModel;
import java.awt.image.MemoryImageSource;

import java.io.File;

import ResourceManager.*;

/*=======================================================================*/
/* Copyright (c) 1999 by G. Brannon Smith -- All Rights Reserved         */
/*=======================================================================*/

/*=======================================================================*/
/*
 * $Log: IconFamilyResourceHandler.java,v $
 * Revision 1.5  1999/10/19 06:00:46  gbsmith
 * Overrode 'about' method. Added copyright notice.
 *
 * Revision 1.4  1999/10/18 03:00:29  gbsmith
 * Added code for icon name collecting. If a resource of given type has
 * no name, matching resources of other types are checked for a name.
 *
 * Revision 1.3  1999/10/17 23:13:38  gbsmith
 * Now gets color models from palette methods.
 *
 * Revision 1.2  1999/10/17 20:32:04  gbsmith
 * Made needed color models once to share throughout instance. Added "n/a"
 * text for unavailable Icon images. Made class imports more explicit.
 *
 * Revision 1.1  1999/10/16 02:20:06  gbsmith
 * Initial revision
 *
 */


/*=======================================================================*/
public class IconFamilyResourceHandler extends GBS_ImageResourceHandler
{
   /*--- Data -----------------------------------------------------------*/
   IndexColorModel icm16, icm256;

   JList resList;
   JTable resTable;
   private static final String[] columnNames =
   { "ResID", "Name", "Size",
     "ICN#", "Imask", "icl4", "icl8", "ics#", "imask", "ics4", "ics8"};

   TableCellRenderer renderer = new IconRenderer();

   Image ICNs[];
   Image ICN_masks[];
   Image icl4s[];
   Image icl8s[];
   Image icss[];
   Image ics_masks[];
   Image ics4s[];
   Image ics8s[];

   String icon_names[];

   String mytypes[] = {"ICN#", "icl4", "icl8", "ics#", "ics4", "ics8"};

   /*------ RCS ---------------------------------------------------------*/
   static final String rcsid = "$Id: IconFamilyResourceHandler.java,v 1.5 1999/10/19 06:00:46 gbsmith Exp $";

   /*--- Methods --------------------------------------------------------*/
   public String[] getTypes()
   {
      return mytypes;
   }

   /*--------------------------------------------------------------------*/
   public void init( )
   {
      /*
        This will be a multi-type handler that will access all other types
        it handles when anyone is called. It will match and display the
        corresponding resources together.

        Must figure out which type we were called with,
        Process it THEN
        Fetch and Process match from OTHER types.
      */

      Resource myResArray[] = resData.getResArray();
      Resource currentRes;

      // Allocate image arrays
      ICNs      = new Image[myResArray.length];
      ICN_masks = new Image[myResArray.length];
      icl4s     = new Image[myResArray.length];
      icl8s     = new Image[myResArray.length];
      icss      = new Image[myResArray.length];
      ics_masks = new Image[myResArray.length];
      ics4s     = new Image[myResArray.length];
      ics8s     = new Image[myResArray.length];

      icon_names = new String[myResArray.length];

      // Make the color models
      icm16  = MacStandard16Palette.getColorModel();
      icm256 = MacStandard256Palette.getColorModel();

      for( int i = 0; i < myResArray.length; i++)
      {
         icon_names[i] = null;
         currentRes    = myResArray[i];
         process(resData.getID(), i, currentRes.getData());
         icon_names[i] = currentRes.getName();

         for(int t = 0; t < mytypes.length; t++)
         {
            if(mytypes[t].compareTo(resData.getID()) == 0) continue;
            if(resMod.contains(mytypes[t], myResArray[i].getID()))
            {
               currentRes =
                  resMod.getResource(mytypes[t], myResArray[i].getID());
               process(mytypes[t], i, currentRes.getData());
               if(icon_names[i] == null) icon_names[i] = currentRes.getName();
            }
         }
      }
   }

   /*--------------------------------------------------------------------*/
   private void process(String type, int index, byte rawData[])
   {
      if(type.compareTo("ICN#") == 0)
      {
         ICNs[index] = process_ICN( rawData );
         ICN_masks[index] = process_ICN_mask( rawData );
      }

      if(type.compareTo("icl4") == 0)
         icl4s[index] = process_icl4( rawData );

      if(type.compareTo("icl8") == 0)
         icl8s[index] = process_icl8( rawData );

      if(type.compareTo("ics#") == 0)
      {
         icss[index]      = process_ics( rawData );
         ics_masks[index] = process_ics_mask( rawData );
      }

      if(type.compareTo("ics4") == 0)
         ics4s[index] = process_ics4( rawData );

      if(type.compareTo("ics8") == 0)
         ics8s[index] = process_ics8( rawData );
   }

   /*--------------------------------------------------------------------*/
   private Image process_ICN( byte rawData[] )
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
   private Image process_ICN_mask( byte rawData[] )
   {
      MemoryImageSource mis;
      byte maskData[];
      int i, j, b;

      // Grab mask data
      maskData = new byte[1024];
      for ( j = 0; j < 128; j++)
         for ( b = 0; b < 8; b++)
            maskData[j*8+b] = (byte)((rawData[j + 128] &
                                      (0x80 >>> b)) > 0? 0xFF: 0x00);

      mis = new MemoryImageSource(32, 32, icm16, maskData, 0, 32);
      return createImage(mis);
   }

   /*--------------------------------------------------------------------*/
   private Image process_icl4( byte rawData[] )
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
   private Image process_icl8( byte rawData[] )
   {
      MemoryImageSource mis;

      mis = new MemoryImageSource(32, 32, icm256, rawData, 0, 32);
      return createImage(mis);
   }

   /*--------------------------------------------------------------------*/
   private Image process_ics( byte rawData[] )
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
   private Image process_ics_mask( byte rawData[] )
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
   private Image process_ics4( byte rawData[] )
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
   private Image process_ics8( byte rawData[] )
   {
      MemoryImageSource mis =
         new MemoryImageSource(16, 16, icm256, rawData, 0, 16);
      return createImage(mis);
   }

   /*--------------------------------------------------------------------*/
   public void display( )
   {
      TableColumn tc;
      DefaultTableModel tmpModel;
      Image currentImg;

      Resource myResArray[] = resData.getResArray();
      tmpModel = new DefaultTableModel( columnNames, myResArray.length );

      for( int i = 0; i < myResArray.length; i++)
      {
         // "ResID", "Name", "Size" -------------------------------------
         tmpModel.setValueAt( new Short(myResArray[i].getID()),  i,  0 );
         tmpModel.setValueAt( icon_names[i],                     i,  1 );
         tmpModel.setValueAt( new Integer(myResArray[i].size()), i,  2 );
         //--------------------------------------------------------------

         // "ICN#", "Imask", "icl4", "icl8" -----------------------------
         currentImg = ICNs[i];
         if(currentImg != null) tmpModel.setValueAt(currentImg,  i,  3 );
         else                   tmpModel.setValueAt("n/a",       i,  3 );

         currentImg = ICN_masks[i];
         if(currentImg != null) tmpModel.setValueAt(currentImg,  i,  4 );
         else                   tmpModel.setValueAt("n/a",       i,  4 );

         currentImg = icl4s[i];
         if(currentImg != null) tmpModel.setValueAt(currentImg,  i,  5 );
         else                   tmpModel.setValueAt("n/a",       i,  5 );

         currentImg = icl8s[i];
         if(currentImg != null) tmpModel.setValueAt(currentImg,  i,  6 );
         else                   tmpModel.setValueAt("n/a",       i,  6 );
         //--------------------------------------------------------------

         // "ics#", "imask", "ics4", "ics8" -----------------------------
         currentImg = icss[i];
         if(currentImg != null) tmpModel.setValueAt(currentImg,  i,  7 );
         else                   tmpModel.setValueAt("n/a",       i,  7 );

         currentImg = ics_masks[i];
         if(currentImg != null) tmpModel.setValueAt(currentImg,  i,  8 );
         else                   tmpModel.setValueAt("n/a",       i,  8 );

         currentImg = ics4s[i];
         if(currentImg != null) tmpModel.setValueAt(currentImg,  i,  9 );
         else                   tmpModel.setValueAt("n/a",       i,  9 );

         currentImg = ics8s[i];
         if(currentImg != null) tmpModel.setValueAt(currentImg,  i, 10 );
         else                   tmpModel.setValueAt("n/a",       i, 10 );
         //--------------------------------------------------------------
      }

      resTable = new JTable(tmpModel);
      resTable.setRowHeight(36);

      for(int cn = 3; cn < columnNames.length; cn++)
      {
         tc = resTable.getColumn(columnNames[cn]);
         tc.setCellRenderer(renderer);
      }

      JScrollPane rtsp = new JScrollPane(resTable);
      setLayout( new BorderLayout() );
      add(rtsp, "Center");
   }

   /*--------------------------------------------------------------------*/
   public String[] about( )
   {
      String[] pluginfo =
      { "IconFamilyResourceHandler",
        "v1.0",
        "by G. Brannon Smith",
        " ",
        "This plugin handles types that are part of the standard",
        "Macintosh Icon family, registering itself for all such",
        "types. Other types in the family are also grabbed and",
        "presented regardless of which type it was called with."
      };

      return pluginfo;
   }

   /*--------------------------------------------------------------------*/
   public void save( File savedir )
   {
      StringBuffer tmpfilename;
      String filename;
      String saveType;
      Image imgToSave[];

      if(resData == null)
      {
         System.err.println("ERROR: No resources to save");
         return;
      }

      Resource myResArray[] = resData.getResArray();
      saveType = resData.getID();

      if(saveType.compareTo("ICN#") == 0) imgToSave = ICNs;
      else if(saveType.compareTo("icl4") == 0) imgToSave = icl4s;
      else if(saveType.compareTo("icl8") == 0) imgToSave = icl8s;
      else if(saveType.compareTo("ics#") == 0) imgToSave = icss;
      else if(saveType.compareTo("ics4") == 0) imgToSave = ics4s;
      else if(saveType.compareTo("ics8") == 0) imgToSave = ics8s;
      else return;

      System.out.println("Saving resources of type \'" + saveType + "\'");
      for(int i=0; i < myResArray.length; i++)
      {
         tmpfilename = new StringBuffer(savedir.getPath());
         tmpfilename.append( File.separator + myResArray[i].getID() );
         if(icon_names[i] != null)
            tmpfilename.append("_" + myResArray[i].getName());
         tmpfilename.append(".xpm");
         filename = tmpfilename.toString().replace(' ', '_');

         try
         {
            Jimi.putImage(imgToSave[i], filename);
         } catch (Exception whatever) {
            System.err.println("ERROR: While saving, got exception " + whatever );
         }
      }

      // Do masks if there are any for given type...
      if(saveType.compareTo("ICN#") == 0 ||
         saveType.compareTo("ics#") == 0)
      {
         if(saveType.compareTo("ICN#") == 0) imgToSave = ICN_masks;
         if(saveType.compareTo("ics#") == 0) imgToSave = ics_masks;

         System.out.println("Saving masks of type \'" + saveType + "\'");
         for(int i=0; i < myResArray.length; i++)
         {
            tmpfilename = new StringBuffer(savedir.getPath());
            tmpfilename.append( File.separator + myResArray[i].getID() );
            if(icon_names[i] != null)
               tmpfilename.append("_" + myResArray[i].getName());
            tmpfilename.append("_mask");
            tmpfilename.append(".xpm");
            filename = tmpfilename.toString().replace(' ', '_');

            try
            {
               Jimi.putImage(imgToSave[i], filename);
            } catch (Exception whatever) {
               System.err.println("ERROR: While saving, got exception " + whatever );
            }
         }
      }
      //---------------------------------------------------------------------
   }
}
