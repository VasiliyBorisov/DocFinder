package docfinder;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.regex.Pattern;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.DefaultCaret;

class DocFinderRun extends JFrame {
	private static final long serialVersionUID = 1L;
	List<File> files = new ArrayList<>();
	File dirName;
	DocFinderRun encObj = this;
	static JProgressBar pb = new JProgressBar();
	private static final String FIND_BTN_TEXT = "Запуск"; 
	
	public static void setPBValue (int value) {
		pb.setValue(value);
		System.out.println(value);
	}
	
    public static void main (String[] args) {
    	new DocFinderRun().buildGui(FIND_BTN_TEXT);
    }
    
    public void buildGui (String title) {
    	setTitle(title);
    	setDefaultCloseOperation(EXIT_ON_CLOSE);
    	JButton btnBrowse = new JButton("Выбрать папку");
    	JButton btnFind = new JButton(FIND_BTN_TEXT);
    	JRadioButton btnCopy = new JRadioButton("Скопировать или");
    	JRadioButton btnMove = new JRadioButton("Переместить в подпапку");
    	btnCopy.setSelected(true);
    	ButtonGroup bGroup = new ButtonGroup();
    	bGroup.add(btnCopy);
    	bGroup.add(btnMove);
    	JTextField redTF = new JTextField("0", 3);
    	JTextField greenTF = new JTextField("0", 3);
    	JTextField blueTF = new JTextField("0", 3);
    	JTextField subDirTF = new JTextField("docImg", 10);
    	JTextField deltaTF = new JTextField("10", 3);
    	JLabel deltaLabel = new JLabel("Дельта:");
    	JLabel rLabel = new JLabel("Значения от 0 до 255. Красный:");
    	JLabel gLabel = new JLabel("Зеленый:");
    	JLabel bLabel = new JLabel("Синий:");
    	
    	JTextArea text = new JTextArea();
    	DefaultCaret caret = (DefaultCaret) text.getCaret();
    	caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
    	
    	text.setRows(20);
    	text.setColumns(60);
    	JCheckBox subDirCB = new JCheckBox("Включать подпапки");

    	JLabel label = new JLabel("Не выбрано");
    	JLabel about = new JLabel("\u00A9 Мария Борисова 2023");
    	JFileChooser fch = new JFileChooser();
    	
    	fch.setDialogTitle("Выбор папки для поиска");
    	fch.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
    	fch.setAcceptAllFileFilterUsed(false);
    	fch.setFileFilter(new FileFilter() {
    		@Override
    		public String getDescription() {
    			return "Jpeg and PNG images";
    		}
    		@Override
    		public boolean accept(File f) {
    			if (f.isDirectory())
    				return true;
    			return Pattern.compile("^.*\\.(jpe?g|png)$", Pattern.CASE_INSENSITIVE).matcher(f.getName()).find();
    		}
    	});
    	
    	btnBrowse.addActionListener(new ActionListener() {
    		@Override
    		public void actionPerformed(ActionEvent e) {
    			int result = fch.showOpenDialog(encObj);
    			if (result == JFileChooser.APPROVE_OPTION) {
    				SwingUtilities.invokeLater(new Runnable() {
    					@Override
    					public void run() {
    						text.setText("");
    						files.clear();
    						btnFind.setEnabled(true);
    						dirName=fch.getSelectedFile();
    						if (dirName.isFile()) dirName = dirName.getParentFile();
							
							Queue<File> fileTree = new PriorityQueue<>();
							File currentFile;
							Collections.addAll(fileTree, dirName.listFiles());
							while (!fileTree.isEmpty()) {
							    currentFile = fileTree.remove();
							    if(subDirCB.isSelected() && currentFile.isDirectory()){
							        Collections.addAll(fileTree, currentFile.listFiles());
							    } else {
							    	if (currentFile.getName().toString().matches("(?i)^.*\\.(jpe?g|png)$"))
							    		files.add(currentFile);
							    }
							}
//    						files = dirName.listFiles((file) -> file.getName().toString().matches("(?i)^.*\\.(jpe?g|png)$"));
    						for (File fName : files)
    							text.append(String.format("%s\n", fName));
    						text.append(String.format("Будет проанализировано файлов: %d\n", files.size()));
    						label.setText(dirName.toString());
    					}
    				});
    				
    				
    			}
    		}
    	});
    	
    	btnFind.addActionListener((e) -> { 
    		new SwingWorker<Void, Integer>() {
			@Override
			protected Void doInBackground() throws Exception {
				btnFind.setText("Поиск");
				btnFind.setEnabled(false);
				int delta, redOfset, greenOfset, blueOfset;
				try {
					delta = Integer.parseInt(deltaTF.getText());
					redOfset = Integer.parseInt(redTF.getText());
					greenOfset = Integer.parseInt(greenTF.getText());
					blueOfset = Integer.parseInt(blueTF.getText());
				} catch (NumberFormatException e) {
					delta = 5;
					redOfset = 0;
					greenOfset = 0;
					blueOfset = 0;
				}
				
				File destDir = new File(dirName.toString() + "/" + subDirTF.getText());
				if (!destDir.exists()) destDir.mkdir();
				int c=0;
				for (int i = 0; i < files.size(); i++) {
					DocFinder df1 = new DocFinder(files.get(i), delta, redOfset, greenOfset, blueOfset);
//					DocFinder df2 = new DocFinder(files[++i], 5);
//					DocFinder df3 = new DocFinder(files[++i], 5);
//					DocFinder df4 = new DocFinder(files[++i], 5);
					new Thread(df1).run();
//					new Thread(df2).run();
//					new Thread(df3).run();
//					new Thread(df4).run();
					
					publish(i+1);
					if (df1.getResult() != null) {
						text.append(String.format("%d ", ++c));
						try {
							File destFile = new File(dirName.toString() + "/" + subDirTF.getText() + "/" + df1.getResult().getName().toString());
							if (btnCopy.isSelected())
								Files.copy(df1.getResult().toPath(), destFile.toPath());
							if (btnMove.isSelected())
								Files.move(df1.getResult().toPath(), destFile.toPath());
							text.append(String.format("%s - ok\n", df1.getResult().toString()));
						} catch (IOException e) {
							text.append(String.format("%s - Не удалось\n", df1.getResult().toString()));
						}
					}
//					if (df2.getResult() != null) text.append(String.format("%s\n", df2.getResult().toString()));
//					if (df3.getResult() != null) text.append(String.format("%s\n", df3.getResult().toString()));
//					if (df4.getResult() != null) text.append(String.format("%s\n", df4.getResult().toString()));
				}
				return null;
			}
			@Override
			protected void done() {
				btnFind.setText(FIND_BTN_TEXT);
				btnFind.setEnabled(true);
				text.append(String.format("Готово\n"));
				
			}
			@Override
			protected void process(List<Integer> chunks) {
				pb.setValue((100 * chunks.get(chunks.size()-1)) / files.size());
			}
    	}.execute();
    	});
    	
    	pb.setMinimum(0);
    	pb.setMaximum(100);
    	pb.setValue(0);
    	pb.setStringPainted(true);

    	JPanel jp1 = new JPanel();
    	JPanel jp2 = new JPanel();
    	JPanel jp3 = new JPanel();
    	JPanel jp31 = new JPanel();
    	JPanel jp32 = new JPanel();
    	BoxLayout bl1 = new BoxLayout(jp1, BoxLayout.X_AXIS);
    	BoxLayout bl2 = new BoxLayout(jp2, BoxLayout.X_AXIS);
    	BoxLayout bl3 = new BoxLayout(jp3, BoxLayout.Y_AXIS);
    	BoxLayout bl31 = new BoxLayout(jp31, BoxLayout.X_AXIS);
    	BoxLayout bl32 = new BoxLayout(jp32, BoxLayout.Y_AXIS);
    	jp1.setLayout(bl1);
    	jp2.setLayout(bl2);
    	jp3.setLayout(bl3);
    	jp31.setLayout(bl31);
    	jp32.setLayout(bl32);
    	
    	jp1.add(subDirCB);
    	jp1.add(btnBrowse);
    	jp1.add(label);
    	
    	JScrollPane sp = new JScrollPane(text);
    	jp2.add(sp);
    	
    	jp31.add(rLabel);
    	jp31.add(redTF);
    	jp31.add(gLabel);
    	jp31.add(greenTF);
    	jp31.add(bLabel);
    	jp31.add(blueTF);
    	jp31.add(deltaLabel);
    	jp31.add(deltaTF);
    	jp31.add(btnCopy);
    	jp31.add(btnMove);
    	jp31.add(subDirTF);
    	jp31.add(btnFind);
    	jp32.add(pb);
    	jp32.add(about);
    	
    	jp3.add(jp31);
    	jp3.add(jp32);
    	
    	
    	getContentPane().add(jp1, BorderLayout.NORTH);
    	getContentPane().add(jp2, BorderLayout.CENTER);
    	getContentPane().add(jp3, BorderLayout.SOUTH);

    	pack();
    	setVisible(true);
    }
}
