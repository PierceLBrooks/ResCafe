/* $Header: /home/gbsmith/projects/MacResReader/ResCafe1.1/src/RCS/IconTable.java,v 1.1 1999/10/27 07:13:41 gbsmith Exp $ */

import com.sun.jimi.core.Jimi; // JIMI - tools for image I/O

import javax.swing.ImageIcon;

import java.util.Hashtable;
import java.io.File;

/*=======================================================================*/
/*
 * $Log: IconTable.java,v $
 * Revision 1.1  1999/10/27 07:13:41  gbsmith
 * Initial revision
 *
 *
 */

/*=======================================================================*/
class IconTable extends Hashtable implements Runnable
{
   /*--- Data -----------------------------------------------------------*/
   String icon_dirname;

   /*----- RCS ----------------------------------------------------------*/
   static final String rcsid = "$Id: IconTable.java,v 1.1 1999/10/27 07:13:41 gbsmith Exp $";

   /*--- Methods --------------------------------------------------------*/
   IconTable(String indir)
   {
      super();
      icon_dirname = indir;
   }

   /*--------------------------------------------------------------------*/
   public void run() // For threaded loading
   {
      load();
   }
   
   /*--------------------------------------------------------------------*/
   void load()
   {
      String icon_filenames[];
      String icontype;
      File   icondir = new File(icon_dirname);

      // Get file list - assume everything in dir is an image
      icon_filenames = icondir.list();

      // Load them and put into hash
      for(int i = 0; i < icon_filenames.length; i++)
      {
         icontype =
            icon_filenames[i].substring(0, icon_filenames[i].lastIndexOf('.'));
         icontype = icontype.replace('_', ' '); // Interpret underscores as spaces

         put(icontype, new ImageIcon(Jimi.getImage(
            icon_dirname + "/" + icon_filenames[i])));
      }
   }
}
