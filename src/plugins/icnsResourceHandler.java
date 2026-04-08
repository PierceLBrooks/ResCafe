/* $Header: /home/gbsmith/projects/ResCafe/ResCafe_devel/src/plugins/RCS/icnsResourceHandler.java,v 1.2 2000/11/27 19:51:37 gbsmith Exp $ */

import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import javax.swing.event.TableModelListener;
import javax.swing.event.TableModelEvent;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Image;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.awt.image.DirectColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.MemoryImageSource;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.Hashtable;

import ResourceManager.*;

/*=======================================================================*/
/* Copyright (c) 2000 by G. Brannon Smith -- All Rights Reserved         */
/*=======================================================================*/

/*=======================================================================*/
/*
 * $Log: icnsResourceHandler.java,v $
 * Revision 1.2  2000/11/27 19:51:37  gbsmith
 * Added a bunch of additional subtypes along with processing code
 * for those subtypes. Still no 32-bit icons or saving though.
 *
 * Revision 1.1  2000/05/24 06:09:41  gbsmith
 * Initial revision
 *
 */


/*------------------------------------------------------------------------
Memory/space saving idea:
   selective allocation and display of type variables based on actual
   encounter w/data so...

   - don't allocate image arrays until subtype first encountered
   - keep track of encountered types in a hash
   - Only create columns for types that have been encountered

   - keep potential column types in separate array
   - Build new column array out of hash
   - Use hash to determine size and contents of new array

-------------------------------------------------------------------------*/


/*=======================================================================*/
public class icnsResourceHandler extends GBS_ImageResourceHandler
{
   /*--- Data -----------------------------------------------------------*/
   private static final String[] basicColumnNames = { "ResID", "Name", "Size" };

   private static final String[] subtypes =
   { "ICN#", "Imask", "icl4", "icl8", "il32", "l8mk",
     "ics#", "imask", "ics4", "ics8", "is32", "s8mk",
     "ich#", "hmask", "ich4", "ich8", "ih32", "h8mk",
     "icm#", "mmask", "icm4", "icm8", "im32"//, "m8mk"
   };

   TableCellRenderer renderer = new IconRenderer();

   //    B&W     Masks        4-bit    8-bit    32-bit
   Image ICNs[], ICN_masks[], icl4s[], icl8s[], il32s[], l8mks[]; // large
   Image icss[], ics_masks[], ics4s[], ics8s[], is32s[], s8mks[]; // small
   Image ichs[], ich_masks[], ich4s[], ich8s[], ih32s[], h8mks[]; // h???

   String types[];

   String mytypes[] = { "icns" };

   Resource myResArray[];
   String icon_names[];
   Hashtable seenSubtypes;
   String[] columnNames;

   /*------ RCS ---------------------------------------------------------*/
   static final String rcsid = "$Id: icnsResourceHandler.java,v 1.2 2000/11/27 19:51:37 gbsmith Exp $";

   /*--- Methods --------------------------------------------------------*/
   public String[] getTypes()
   {
      return mytypes;
   }

   /*--------------------------------------------------------------------*/
   public void init( )
   {
      myResArray = resData.getResArray();
      seenSubtypes = new Hashtable();

      Resource currentRes;

      ICNs = ICN_masks = icl4s = icl8s = il32s = l8mks = null;
      icss = ics_masks = ics4s = ics8s = is32s = s8mks = null;
      ichs = ich_masks = ich4s = ich8s = ih32s = h8mks = null;

      icon_names = new String[myResArray.length];
      types      = new String[myResArray.length];

      for( int i = 0; i < myResArray.length; i++)
      {
         icon_names[i] = null;
         currentRes    = myResArray[i];
         read(i, currentRes.getData());
         icon_names[i] = currentRes.getName();

         // Get resource name from related types
         for(int t = 0; t < subtypes.length; t++)
            if( resMod.contains(subtypes[t], myResArray[i].getID()) &&
                icon_names[i] == null)
            {
               currentRes =
                  resMod.getResource(subtypes[t], myResArray[i].getID());
               if(currentRes != null) icon_names[i] = currentRes.getName();
            }
      }
   }


   /*--------------------------------------------------------------------*/
   private void read(int index, byte rawData[])
   {
      DataInputStream dis =
         new DataInputStream(new ByteArrayInputStream(rawData));
      byte subname[] = new byte[4];
      int fullsize   = 0;
      int subsize    = 0;
      byte subData[];

      StringBuffer tmpnames = new StringBuffer();

      try
      {
         dis.readFully(subname); // skip main type
         fullsize = dis.readInt();
         fullsize -= 8;

         // Loop through subtypes
         while(fullsize > 0)
         {
            dis.readFully(subname);
            subsize = dis.readInt();
            fullsize -= subsize;

            tmpnames.append(new String(subname) + " (" + (subsize-8) + "), ");

            subData = new byte[subsize - 8];
            dis.readFully(subData);
            process(new String(subname), index, subData);
         }

      } catch (IOException ioe) {
      }

      types[index] = new String(tmpnames);
   }

   /*--------------------------------------------------------------------*/
   private void process(String type, int index, byte rawData[])
   {
      if(type.compareTo("ICN#") == 0)
      {
         if(ICNs == null)      ICNs      = new Image[myResArray.length];
         if(ICN_masks == null) ICN_masks = new Image[myResArray.length];
         ICNs[index]      = process_ICN( rawData );
         ICN_masks[index] = process_ICN_mask( rawData );
         seenSubtypes.put(type,    new Boolean(true));
         seenSubtypes.put("Imask", new Boolean(true));
      }

      if(type.compareTo("icl4") == 0)
      {
         if(icl4s == null) icl4s = new Image[myResArray.length];
         icl4s[index] = process_icl4( rawData );
         seenSubtypes.put(type, new Boolean(true));
      }

      if(type.compareTo("icl8") == 0)
      {
         if(icl8s == null) icl8s = new Image[myResArray.length];
         icl8s[index] = process_icl8( rawData );
         seenSubtypes.put(type, new Boolean(true));
      }

      if(type.compareTo("il32") == 0)
      {
         if(il32s == null) il32s = new Image[myResArray.length];
         il32s[index] = process_il32( rawData );
         seenSubtypes.put(type, new Boolean(true));
      }

      if(type.compareTo("l8mk") == 0)
      {
         if(l8mks == null) l8mks = new Image[myResArray.length];
         l8mks[index] = process_l8mk( rawData ); // HEY
         seenSubtypes.put(type, new Boolean(true));
      }

      if(type.compareTo("ics#") == 0)
      {
          if(icss == null)      icss      = new Image[myResArray.length];
          if(ics_masks == null) ics_masks = new Image[myResArray.length];
          icss[index]      = process_ics( rawData );
          ics_masks[index] = process_ics_mask( rawData );
          seenSubtypes.put(type,    new Boolean(true));
          seenSubtypes.put("imask", new Boolean(true));
      }

      if(type.compareTo("ics4") == 0)
      {
         if(ics4s == null) ics4s = new Image[myResArray.length];
         ics4s[index] = process_ics4( rawData );
         seenSubtypes.put(type, new Boolean(true));
      }

      if(type.compareTo("ics8") == 0)
      {
         if(ics8s == null) ics8s = new Image[myResArray.length];
         ics8s[index] = process_ics8( rawData );
         seenSubtypes.put(type, new Boolean(true));
      }

      if(type.compareTo("is32") == 0)
      {
         if(is32s == null) is32s = new Image[myResArray.length];
         is32s[index] = process_is32( rawData );
         seenSubtypes.put(type, new Boolean(true));
      }

      if(type.compareTo("s8mk") == 0)
      {
         if(s8mks == null) s8mks = new Image[myResArray.length];
         s8mks[index] = process_s8mk( rawData ); // HEY
         seenSubtypes.put(type, new Boolean(true));
      }

      if(type.compareTo("ich#") == 0)
      {
          if(ichs == null)      ichs      = new Image[myResArray.length];
          if(ich_masks == null) ich_masks = new Image[myResArray.length];
          ichs[index]      = process_ich( rawData );
          ich_masks[index] = process_ich_mask( rawData );
          seenSubtypes.put(type,    new Boolean(true));
          seenSubtypes.put("hmask", new Boolean(true));
      }

      if(type.compareTo("ich4") == 0)
      {
         if(ich4s == null) ich4s = new Image[myResArray.length];
         ich4s[index] = process_ich4( rawData );
         seenSubtypes.put(type, new Boolean(true));
      }

      if(type.compareTo("ich8") == 0)
      {
         if(ich8s == null) ich8s = new Image[myResArray.length];
         ich8s[index] = process_ich8( rawData );
         seenSubtypes.put(type, new Boolean(true));
      }

      if(type.compareTo("ih32") == 0)
      {
         if(ih32s == null) ih32s = new Image[myResArray.length];
         //ih32s[index] = process_ih32( rawData );
         seenSubtypes.put(type, new Boolean(true));
      }

      if(type.compareTo("h8mk") == 0)
      {
         if(h8mks == null) h8mks = new Image[myResArray.length];
         //h8mks[index] = process_h8mk( rawData ); // HEY
         seenSubtypes.put(type, new Boolean(true));
      }
   }

   /*--------------------------------------------------------------------*/
   public void display( )
   {
      TableColumn tc;
      DefaultTableModel tmpModel;
      Image currentImg;

      Resource myResArray[] = resData.getResArray();

      // Build new array of column names
      columnNames = new String[3 + seenSubtypes.size()];
      columnNames[0] = basicColumnNames[0];
      columnNames[1] = basicColumnNames[1];
      columnNames[2] = basicColumnNames[2];

      int cn=3;
      for(int st=0; st < subtypes.length; st++)
      {
         if(seenSubtypes.containsKey(subtypes[st]))
            columnNames[cn++] = subtypes[st];
      }

      tmpModel = new DefaultTableModel( columnNames, myResArray.length );

      for( int i = 0; i < myResArray.length; i++)
      {
         // "ResID", "Name", "Size" -------------------------------------
         tmpModel.setValueAt( new Short(myResArray[i].getID()),  i,  0 );
         tmpModel.setValueAt( icon_names[i],                     i,  1 );
         tmpModel.setValueAt( new Integer(myResArray[i].size()), i,  2 );
         //--------------------------------------------------------------

         // Icons -------------------------------------------------------
         for(cn=3; cn < columnNames.length; cn++)
         {
            tmpModel.setValueAt(fetchIcon(i, cn), i, cn);
         }
      }

      resTable = new JTable(tmpModel);
      resTable.setRowHeight(36);

      // IMPORTANT! Add decorator first THEN set Icon Renderers afterwards.
      //            This prevents the renders from being trampled by the
      //            Decorator model.
      //

      addDecorator();
      for(cn = 3; cn < columnNames.length; cn++)
      {
         tc = resTable.getColumn(columnNames[cn]);
         tc.setCellRenderer(renderer);
      }
      optimizeColumnWidth();

      JScrollPane rtsp = new JScrollPane(resTable);
      setLayout( new BorderLayout() );
      add(rtsp, "Center");
   }

   /*--------------------------------------------------------------------*/
   private Object fetchIcon(int row, int col)
   {
      String colname = columnNames[col];
      Image outimg;

      if(colname == null) outimg = null;
      else if(colname.compareTo("ICN#")  == 0) outimg = ICNs[row];
      else if(colname.compareTo("Imask") == 0) outimg = ICN_masks[row];
      else if(colname.compareTo("icl4")  == 0) outimg = icl4s[row];
      else if(colname.compareTo("icl8")  == 0) outimg = icl8s[row];
      else if(colname.compareTo("il32")  == 0) outimg = il32s[row];
      else if(colname.compareTo("l8mk")  == 0) outimg = l8mks[row];
      else if(colname.compareTo("ics#")  == 0) outimg = icss[row];
      else if(colname.compareTo("imask") == 0) outimg = ics_masks[row];
      else if(colname.compareTo("ics4")  == 0) outimg = ics4s[row];
      else if(colname.compareTo("ics8")  == 0) outimg = ics8s[row];
      else if(colname.compareTo("is32")  == 0) outimg = is32s[row];
      else if(colname.compareTo("s8mk")  == 0) outimg = s8mks[row];
      else if(colname.compareTo("ich#")  == 0) outimg = ichs[row];
      else if(colname.compareTo("hmask") == 0) outimg = ich_masks[row];
      else if(colname.compareTo("ich4")  == 0) outimg = ich4s[row];
      else if(colname.compareTo("ich8")  == 0) outimg = ich8s[row];
      else if(colname.compareTo("ih32")  == 0) outimg = ih32s[row];
      else if(colname.compareTo("h8mk")  == 0) outimg = h8mks[row];
      else outimg = null;

      if(outimg == null) return "n/a";
      else return outimg;
   }

   /*--------------------------------------------------------------------*/
   protected Image process_il32( byte rawData[] ) { return null; }
   
   /*--------------------------------------------------------------------*/
   protected Image process_is32( byte rawData[] ) { return null; }

   /*--------------------------------------------------------------------*/
   protected Image process_ich( byte rawData[] )
   {
      return process_1bit( rawData, 48, false );
   }

   /*--------------------------------------------------------------------*/
   protected Image process_ich_mask( byte rawData[] )
   {
      return process_1bit( rawData, 48, true );
   }
   
   /*--------------------------------------------------------------------*/
   protected Image process_ich4( byte rawData[] )
   {
      return process_4bit( rawData, 48 );
   }

   /*--------------------------------------------------------------------*/
   protected Image process_ich8( byte rawData[] )
   {
      MemoryImageSource mis =
         new MemoryImageSource(48, 48, icm256, rawData, 0, 48);
      return createImage(mis);
   }

   /*--------------------------------------------------------------------*/
   protected Image process_l8mk( byte rawData[] )
   {
      // This is supposed to be an 8-bit mask - an alpha channel, 
      // BUT notice how we don't use an alpha channel below treating it
      // intstead like an 8-bit grey image. Could this be a problem later
      // on like when we need to actually apply the mask to the image to
      // save it to file?
      MemoryImageSource mis =
         new MemoryImageSource(32, 32,             //  Red  Green  Blue
                               new DirectColorModel(8, 0xFF, 0xFF, 0xFF),
                               rawData, 0, 32);
      return createImage(mis);
   }
   
   /*--------------------------------------------------------------------*/
   protected Image process_s8mk( byte rawData[] ) 
   {
      // Can create icon directly from data
      MemoryImageSource mis =
         new MemoryImageSource(16, 16, 
                               new DirectColorModel(8, 0xFF, 0xFF, 0xFF),
                               //  DirectColorModel(8, 0xFF, 0xFF, 0xFF, 0xFF),
                               rawData, 0, 16);
      return createImage(mis);
   }
   
   /*--------------------------------------------------------------------*/
   public void save ( File savedir)
   {
      StringBuffer tmpfilename;
      String filename, saveType;
      Resource myResArray[];

      if(resData == null)
      {
         System.err.println("ERROR: No resources to save");
         return;
      }

      saveType   = resData.getID();
      myResArray = resData.getResArray();

      System.out.println("Saving resources of type \'" +
                         saveType + "\' as bytes");
      for(int i=0; i < myResArray.length; i++)
      {
         tmpfilename = new StringBuffer(savedir.getPath());
         tmpfilename.append( File.separator + myResArray[i].getID() );
         if(myResArray[i].getName() != null)
            tmpfilename.append("_" + myResArray[i].getName().
                               replace(' ', '_').
                               replace(File.separatorChar, '+'));
         tmpfilename.append(".raw");
         filename = tmpfilename.toString().replace(' ', '_');

         try
         {
            FileOutputStream fos = new FileOutputStream(filename);
            fos.write(myResArray[i].getData());
         } catch (Exception whatever) {
            System.err.println("ERROR: Got exception " + whatever );
         }
      }
   }


   /*--------------------------------------------------------------------*/
   public String[] about( )
   {
      String[] pluginfo =
      { "icnsResourceHandler",
        "v0.9",
        "by G. Brannon Smith",
        " ",
        "This plugin attempts to handle the newer icns 32-bit icon."
      };

      return pluginfo;
   }
}

