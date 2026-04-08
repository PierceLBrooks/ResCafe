/* $Header: /home/gbsmith/projects/MacResReader/ResCafe_1.0/src/RCS/jMainResourceView.java,v 1.8 1999/10/21 23:44:26 gbsmith Exp $ */

import com.sun.jimi.core.Jimi; // JIMI - tools for image I/O

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.io.File;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Observable;
import java.util.Observer;

import ResourceManager.*;

/*=======================================================================*/
/*
 * $Log: jMainResourceView.java,v $
 * Revision 1.8  1999/10/21 23:44:26  gbsmith
 * Added Copyright notice. Made class imports more explicit.
 * Changed setFileController code a little.
 *
 * Revision 1.7  1999/10/19 06:14:32  gbsmith
 * Added help menu with items for requesting info about the application
 * and about the currently displayed plugin handler.
 *
 * Revision 1.6  1999/10/18 00:08:12  gbsmith
 * Added menu items and calls for requesting listings of supported
 * types and registered handlers.
 *
 * Revision 1.5  1999/10/16 02:19:17  gbsmith
 * Added menu and items to control handlers - one to Rescan or reload them (so
 * they can be changed while running) and one to list the handlers and type
 * (not yet implemented)
 *
 * Revision 1.4  1999/10/13 23:42:47  gbsmith
 * Updated to conform with new MacResourceHandler calls. Removed useless
 * 'Save' menu item.
 *
 * Revision 1.3  1999/10/04 21:42:57  gbsmith
 * Made class imports more explicit. Added save and show items to menu.
 * Converted to SplitPane layout. A few other adjustments...
 *
 * Revision 1.2  1999/10/02 03:55:36  gbsmith
 * Moved main method and MVC management out to a top level app class,
 * ResCafe.
 *
 * Revision 1.1  1999/09/30 05:12:56  gbsmith
 * Initial revision
 *
 */

/*=======================================================================*/
/* Copyright (c) 1999 by G. Brannon Smith -- All Rights Reserved         */
/*=======================================================================*/

/*=======================================================================*/
public class jMainResourceView extends JFrame implements Observer
{
   /*--- Data -----------------------------------------------------------*/
   /*------ GUI ---------------------------------------------------------*/
   // Menu stuff
   JMenuBar mbar;
   JMenu fileMenu, typeMenu, handlerMenu, helpMenu;
   JMenuItem openItem, saveAllItem, saveHandledItem, saveCurrentItem, quitItem;
   JMenuItem showAllItem, showHandledItem;
   JMenuItem rescanItem, listTypeItem, listHandlerItem;
   JMenuItem aboutAppItem, aboutPlugItem;

   // Panel stuff
   JPanel typePanel, handlerPanel, typeLabPanel, handlerLabPanel, filePanel;
   JLabel typeLab, handlerLab, fnlab;

   JSplitPane jsp;
   JScrollPane lsp;
   JList myTypeList;

   MacResourceHandler currentHandler;
   String currentType;
   Hashtable icons;

   /*------ Models --------------------------------------------------------*/
   ResourceModel resmod;
   HandlerTable handlers;

   /*------ Controllers ---------------------------------------------------*/
   FileController     flistener;
   WindowController   locWinListener;
   MenuItemController locMenuListener;
   TypeListController locListListener;

   /*------ RCS -----------------------------------------------------------*/
   static final String rcsid =
   "$Id: jMainResourceView.java,v 1.8 1999/10/21 23:44:26 gbsmith Exp $";

   /*--- Methods ----------------------------------------------------------*/
   public jMainResourceView(String frameTitle)
   {
      super(frameTitle);
      Container contentPane = getContentPane();

      /* Set up menubar and menus -----------------------------------------*/
      mbar = new JMenuBar();

      /*-------------------------------------------------------------------*/
      fileMenu = new JMenu("File", true);
      /*-------------------------------------------------------------------*/
      fileMenu.add(openItem        = new JMenuItem("Open..."));
      fileMenu.add(saveAllItem     = new JMenuItem("Save All"));
      fileMenu.add(saveHandledItem = new JMenuItem("Save Handled"));
      fileMenu.add(saveCurrentItem = new JMenuItem("Save Current"));
      fileMenu.addSeparator();
      fileMenu.add(quitItem        = new JMenuItem("Quit"));

      /*-------------------------------------------------------------------*/
      typeMenu = new JMenu("Type", true);
      /*-------------------------------------------------------------------*/
      typeMenu.add(showAllItem     = new JMenuItem("Show All Types"));
      typeMenu.add(showHandledItem = new JMenuItem("Show Handled Types"));

      /*-------------------------------------------------------------------*/
      handlerMenu = new JMenu("Handlers", true);
      /*-------------------------------------------------------------------*/
      handlerMenu.add(rescanItem   = new JMenuItem("Rescan Handlers"));
      handlerMenu.add(listHandlerItem = new JMenuItem("List Handlers by Type"));
      handlerMenu.add(listTypeItem = new JMenuItem("List Types by Handler"));
      // These names and descriptions can be confusing

      /*-------------------------------------------------------------------*/
      helpMenu = new JMenu("Help", true);
      /*-------------------------------------------------------------------*/
      helpMenu.add(aboutAppItem  = new JMenuItem("About ResCafé"));
      helpMenu.add(aboutPlugItem = new JMenuItem("About Plugin"));

      mbar.add(fileMenu);
      mbar.add(typeMenu);
      mbar.add(handlerMenu);
      mbar.add(helpMenu); // Wish this could go at extreme right of MenuBar...

      setJMenuBar(mbar);

      /* Setup Panels -----------------------------------------------------*/
      typePanel    = new JPanel();
      handlerPanel = new JPanel();
      contentPane.add(jsp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                                           false, typePanel, handlerPanel ),
                      "Center");
      jsp.setOneTouchExpandable(true);

      /* Set up list of Resource Types found ------------------------------*/
      typePanel.setLayout(new BorderLayout());

      typeLabPanel = new JPanel();
      typeLabPanel.setBorder(BorderFactory.createRaisedBevelBorder());
      //typeLabPanel.setBorder(BorderFactory.createEtchedBorder(
      //   getBackground().brighter(), getBackground().darker()));

      typePanel.add(typeLabPanel, "North");

      typeLab  = new JLabel("Resource Types");
      typeLabPanel.add(typeLab);

      myTypeList = new JList();
      lsp = new JScrollPane(myTypeList);
      typePanel.add(lsp, "Center");

      /* Set up area for Resource Handler display -------------------------*/
      handlerPanel.setLayout(new BorderLayout());

      handlerLabPanel = new JPanel();
      handlerLabPanel.setBorder(BorderFactory.createRaisedBevelBorder());
      //handlerLabPanel.setBorder(BorderFactory.createEtchedBorder(
      //   getBackground().brighter(), getBackground().darker()));

      handlerPanel.add(handlerLabPanel, "North");

      handlerLab = new JLabel("Resources");
      handlerLabPanel.add(handlerLab);

      /* Setup file name display ------------------------------------------*/
      filePanel = new JPanel();
      filePanel.setBorder(BorderFactory.createEtchedBorder(
         getBackground().brighter(), getBackground().darker()));
      fnlab = new JLabel("*no file loaded*");
      filePanel.add(fnlab);
      contentPane.add(filePanel, "South");

      /* Setup event listeners local to this view -------------------------*/
      locMenuListener = new MenuItemController();
      quitItem.addActionListener(locMenuListener);

      showAllItem.addActionListener(locMenuListener);
      showHandledItem.addActionListener(locMenuListener);

      rescanItem.addActionListener(locMenuListener);
      listHandlerItem.addActionListener(locMenuListener);
      listTypeItem.addActionListener(locMenuListener);

      aboutAppItem.addActionListener(locMenuListener);
      aboutPlugItem.addActionListener(locMenuListener);

      addWindowListener(locWinListener = new WindowController());

      myTypeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      myTypeList.addListSelectionListener(locListListener =
                                          new TypeListController());

      /* Reset split pane to nicer proportions ----------------------------*/
      jsp.setDividerLocation(jsp.getMinimumDividerLocation());

      loadImages();
      currentHandler = null;
      currentType    = null;
   }

   /*--------------------------------------------------------------------*/
   public void setResModel(ResourceModel inmod)
   {
      resmod = inmod;
      resmod.addObserver(this);
   }

   /*--------------------------------------------------------------------*/
   public void setHandlers(HandlerTable intab)
   {
      handlers = intab;
   }

   /*--------------------------------------------------------------------*/
   public void setFileController(FileController infc)
   {
      // NOTE: this is not "addFileController" because it is assumed that
      //       there is just one of them
      if(flistener == infc) return;

      //This code could cause problems on Solaris
      if(flistener != null)
      {
         openItem.removeActionListener(flistener);
         saveAllItem.removeActionListener(flistener);
         saveHandledItem.removeActionListener(flistener);
         saveCurrentItem.removeActionListener(flistener);
      }

      flistener = infc;

      openItem.addActionListener(flistener);
      saveAllItem.addActionListener(flistener);
      saveHandledItem.addActionListener(flistener);
      saveCurrentItem.addActionListener(flistener);

      flistener.setCurrentType( null );
   }

   /*--------------------------------------------------------------------*/
   public String getCurrentType()
   {
      return currentType;
   }

   /*--------------------------------------------------------------------*/
   public void update(Observable o, Object arg)
   {
      fnlab.setText(resmod.getFilename());

      // Clean out old display
      if(currentHandler != null) handlerPanel.remove(currentHandler);
      currentHandler = null;
      if(flistener != null) flistener.setCurrentType(null);

      showAllTypes();
   }

   /*--------------------------------------------------------------------*/
   void showAllTypes()
   {
      MacResTypeListModel tmpModel = new MacResTypeListModel(icons);
      ListCellRenderer renderer = new MacResTypeListCellRenderer();
      String s;

      Enumeration typeKeys = resmod.getTypes();
      if(typeKeys == null) return;
      while( typeKeys.hasMoreElements() )
      {
         s = (String)typeKeys.nextElement();
         tmpModel.addInOrder(s);
      }

      myTypeList.setModel(tmpModel);
      myTypeList.setCellRenderer(renderer);
      repaint();
   }

   /*--------------------------------------------------------------------*/
   void showHandledTypes()
   {
      MacResTypeListModel tmpModel = new MacResTypeListModel(icons);
      ListCellRenderer renderer = new MacResTypeListCellRenderer();
      String s;

      Enumeration typeKeys = resmod.getTypes();
      if(typeKeys == null) return;
      while( typeKeys.hasMoreElements() )
      {
         s = (String)typeKeys.nextElement();
         if(handlers.canHandleType(s))
            tmpModel.addInOrder(s);
      }

      myTypeList.setModel(tmpModel);
      myTypeList.setCellRenderer(renderer);
      repaint();
   }

   /*--------------------------------------------------------------------*/
   private void loadImages()
   {
      icons = new Hashtable();

      String icon_dirname = "icons";
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
         //System.out.println("Got icon for type \"" + icontype + "\"");

         icons.put(icontype,
                   new ImageIcon(Jimi.getImage(
                      icon_dirname + "/" + icon_filenames[i])));
      }
   }


   /*--------------------------------------------------------------------*/
   private void updateResType()
   {
      // Figure out what type was selected...
      // This seems kinda awkward - is there a simpler way?
      if(!myTypeList.isSelectionEmpty())
      {
         Object whichTypeObj[] = (Object[])myTypeList.getModel().
            getElementAt(myTypeList.getMaxSelectionIndex());

         String whichType = (String)whichTypeObj[0];
         handleType(whichType);
      }
   }

   /*--------------------------------------------------------------------*/
   private void handleType(String whichType)
   {
      currentType = whichType;
      if(flistener != null) flistener.setCurrentType(whichType);

      if(currentHandler != null) handlerPanel.remove( currentHandler );

      /* This will eventually check the type of handler to use */
      if(handlers.canHandleType(currentType))
      {
         try
         {
            currentHandler =
               (MacResourceHandler)handlers.
               getHandler(currentType).newInstance();
         } catch (Exception e) {
            System.err.println(e);
         }
         handlerLab.setText("" + resmod.getCountOfType(currentType) +
                            " resources of type \'" + currentType +
                            "\' handled by " +
                            currentHandler.getClass().getName());
      } else {
         currentHandler = new DefaultResourceHandler();
         handlerLab.setText("" + resmod.getCountOfType(currentType) +
                            " resources of type \'" + currentType +
                            "\' handled by Default Handler");
      }

      currentHandler.setResData(resmod.getResourceType(currentType));
      currentHandler.setResModel(resmod);
      currentHandler.init();
      currentHandler.display();
      handlerPanel.add(currentHandler, "Center");

      Dimension hpdim = handlerPanel.getSize();
      Dimension hldim = handlerLab.getSize();

      handlerPanel.invalidate ();
      handlerPanel.validate ();
      handlerPanel.repaint ();
   }

   /*--------------------------------------------------------------------*/
   private void aboutResCafe()
   {
      JOptionPane jop = new JOptionPane();
      String message[] = {
         "ResCafé v1.0",
         "by G. Brannon Smith <brannonsmith@yahoo.com>",
         " ",
         "A Java app for rendering and extracting data",
         "from Mac Resource Forks on other platforms -",
         "mainly Linux"
      };

      jop.showMessageDialog(
         this,     // Parent
         message,  // "about message here",
         "About ResCafé",
         JOptionPane.INFORMATION_MESSAGE
         );
   }

   /*--------------------------------------------------------------------*/
   private void aboutCurrentPlugin()
   {
      int t,m;

      JOptionPane jop = new JOptionPane();

      String about[], types[], message[];
      StringBuffer typelist;

      if(currentHandler == null) return;
      else
      {
         types = currentHandler.getTypes();
         about = currentHandler.about();
         if(about == null) about = new String[]{"No information available",
                                                "for current plugin"};
      }

      typelist = new StringBuffer("Handled Types: ");
      for(t = 0; t < types.length ; t++)
      {
         typelist.append("\'" + types[t] + "\'");
         if( t < types.length - 1 ) typelist.append(", ");
      }

      message = new String[about.length + 2];
      for(m = 0; m < about.length; m++) message[m] = about[m];
      message[m++] = " ";
      message[m++] = typelist.toString();

      jop.showMessageDialog(
         this,     // Parent
         message,  // "about message here",
         "About Current Plugin",
         JOptionPane.INFORMATION_MESSAGE
         );
   }

   /*--------------------------------------------------------------------*/
   private void doQuit()
   {
      boolean success = false;

      dispose();
      System.exit(0);
   }


   /*====================================================================*/
   // Below are inner Controller/Listener classes to manage events local
   // to this view. The outside Models & Views are unaffected.
   /*====================================================================*/
   class MenuItemController implements ActionListener
   {
      // Local because it only affects aspects of the local display:
      // *** HOWEVER, perhaps quit portion should not be local
      /*------ RCS ---------------------------------------------------------*/
      final String rcsid = "$Id: jMainResourceView.java,v 1.8 1999/10/21 23:44:26 gbsmith Exp $";

      /*--------------------------------------------------------------------*/
      public void actionPerformed(ActionEvent event)
      {
         JMenuItem item = (JMenuItem)event.getSource();
         if     (item == quitItem)        doQuit();
         else if(item == showAllItem)     showAllTypes();
         else if(item == showHandledItem) showHandledTypes();
         else if(item == rescanItem)      handlers.build();

         // Currently only console versions - not windows
         else if(item == listHandlerItem) handlers.listHandlersbyType();
         else if(item == listTypeItem)    handlers.listTypesbyHandler();

         else if(item == aboutAppItem)    aboutResCafe();
         else if(item == aboutPlugItem)   aboutCurrentPlugin();
         else System.err.println("Invalid Menu Item");
      }
   }

   /*====================================================================*/
   class TypeListController implements ListSelectionListener
   {
      // Local because it only affects position of scroller
      // Detects clicks in the Type List and displays resources of that type

      /*------ RCS ---------------------------------------------------------*/
      final String rcsid = "$Id: jMainResourceView.java,v 1.8 1999/10/21 23:44:26 gbsmith Exp $";

      /*--------------------------------------------------------------------*/
      public void valueChanged(ListSelectionEvent lse)
      {
         if(!lse.getValueIsAdjusting())
         {
            int id_index = lse.getLastIndex();
            updateResType();
         }
      }
   }


   /*====================================================================*/
   class WindowController extends WindowAdapter
   {
      // *** Perhaps this shouldn't be local after all
      /*------ RCS ---------------------------------------------------------*/
      final String rcsid = "$Id: jMainResourceView.java,v 1.8 1999/10/21 23:44:26 gbsmith Exp $";

      /*--------------------------------------------------------------------*/
      public void windowClosing(WindowEvent event)
      {
         doQuit();
      }
   }
}
