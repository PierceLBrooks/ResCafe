/* $Header: /home/gbsmith/projects/ResCafe/ResCafe1.2.5/src/plugins/RCS/icnsResourceHandler.java,v 1.1 2000/05/24 06:09:41 gbsmith Exp $ */

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
   { "ICN#", "Imask", "icl4", "icl8", "il32",
     "ics#", "imask", "ics4", "ics8", "is32" };

   TableCellRenderer renderer = new IconRenderer();

   //    B&W     Masks        4-bit    8-bit    32-bit
   Image ICNs[], ICN_masks[], icl4s[], icl8s[], il32s[]; //l8mk
   Image icss[], ics_masks[], ics4s[], ics8s[], is32s[]; //s8mk

   String types[];

   String mytypes[] = { "icns" };

   Resource myResArray[];
   String icon_names[];
   Hashtable seenSubtypes;
   String[] columnNames;

   /*------ RCS ---------------------------------------------------------*/
   static final String rcsid = "$Id: icnsResourceHandler.java,v 1.1 2000/05/24 06:09:41 gbsmith Exp $";

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

      ICNs      = null;
      ICN_masks = null;
      icl4s     = null;
      icl8s     = null;
      il32s     = null;

      icss      = null;
      ics_masks = null;
      ics4s     = null;
      ics8s     = null;
      is32s     = null;


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
      /*
        il32s     = new Image[myResArray.length];
        is32s     = new Image[myResArray.length];
      */



      if(type.compareTo("ICN#") == 0)
      {
         if(ICNs == null)      ICNs      = new Image[myResArray.length];
         if(ICN_masks == null) ICN_masks = new Image[myResArray.length];
         ICNs[index] = process_ICN( rawData );
         ICN_masks[index] = process_ICN_mask( rawData );
         seenSubtypes.put(type, new Boolean(true));
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
      }

      if(type.compareTo("ics#") == 0)
      {
          if(icss == null)      icss      = new Image[myResArray.length];
          if(ics_masks == null) ics_masks = new Image[myResArray.length];
          icss[index]      = process_ics( rawData );
          ics_masks[index] = process_ics_mask( rawData );
      }

      if(type.compareTo("ics4") == 0)
      {
         if(ics4s == null) ics4s = new Image[myResArray.length];
         ics4s[index] = process_ics4( rawData );
      }

      if(type.compareTo("ics8") == 0)
      {
         if(ics8s == null) ics8s = new Image[myResArray.length];
         ics8s[index] = process_ics8( rawData );
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
            tmpModel.setValueAt(fetchIcon(columnNames[cn], i, cn), i, cn);
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
   private Object fetchIcon(String colname, int row, int col)
   {
      //if(colname.compareTo(null) == 0)
         return "n/a";
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
}

