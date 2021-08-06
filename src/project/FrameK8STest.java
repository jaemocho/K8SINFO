package project;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.FlowLayout;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.BoxLayout;
import javax.swing.JLabel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
//import org.jfree.util.Rotation;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.StringTokenizer;


public class FrameK8STest extends JFrame {
	
    private static final long serialVersionUID = -711163588504124217L;

    
	public static JLabel siteLabel = new JLabel("Select Site");
	public static JTextArea textArea = new JTextArea("hello", 7 ,20);
	public static JPanel leftpanel = new JPanel();
	public static JPanel rightpanel = new JPanel();
	public static JPanel topPanel = new JPanel();
	public static JPanel bottomPanel = new JPanel();
	public static SelectDialog selectDialog;
	public static ChartPanel chartPanel;
	public static Session session;
	public static ChannelExec sftpChannelExec;
	public static Channel channel;
	public static ChannelShell channelShell;
	public static ChannelSftp channelSftp;
	public static ArrayList<String> siteList;
	
	public static String currentSite;
	public static String currentNamespace;
	public static String currentModule;
	public static String currentItem;
	
	public static String currentHostname;
	public static String currentId;
	public static String currentPasswd;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
                    //System.setProperty("java.awt.headless", "true");
					FrameK8STest frame = new FrameK8STest();
					frame.setVisible(true);
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	//PO 조회 Option Dialog
	class SelectDialog extends JDialog{
		 
        private static final long serialVersionUID = -711163588504124217L;

		public String select ="";
		
		SelectDialog(JFrame frame){
			super(frame, "Select Option",true);//끝에 true를 넣으면 modal이 됨
			 //다이알로그는 Frame이 소유함, 소유권자가 있어야함(null가능)
			
			JButton describeButton = new JButton("descirbe");
			JButton jeusOptionButton = new JButton("JEUS Option(Domain.xml)");
			JButton logButton = new JButton("JEUS Server Log (tail -5000)");
			
			
			 setLayout(new FlowLayout());
			 add(describeButton);
			 add(jeusOptionButton);
			 add(logButton);
			 
			  
			 //다이얼로그 버튼에 액션리스너 붙이기, 이벤트 처리 위함
			 //원래 생성한 객체를 addActionListener()안에 넣어야 하지만 익명중첩클래스사용
			 describeButton.addActionListener(
					 new ActionListener() {
					  public void actionPerformed(ActionEvent e) {
						  select = "describe";
						  setVisible(false);// 다이알로그의 오케이버튼 누르면 창 안보이는 이벤트
					  }
			    }
			 );
			 
			 jeusOptionButton.addActionListener(
					 new ActionListener() {
					  public void actionPerformed(ActionEvent e) {
						  select = "jeusoption";
						  setVisible(false);// 다이알로그의 오케이버튼 누르면 창 안보이는 이벤트
					  }
			    }
			 );
			 
			 logButton.addActionListener(
					 new ActionListener() {
					  public void actionPerformed(ActionEvent e) {
						  select = "log";
						  setVisible(false);// 다이알로그의 오케이버튼 누르면 창 안보이는 이벤트
					  }
			    }
			 );
		 }
		 
	}
	
	class ChartPanel extends JPanel{ // 차트 표시 패널
        
        private static final long serialVersionUID = -711163588504124217L;
		
		String podname[];
		float memuse[];
		
		public void paintComponent(Graphics g){
 
			super.paintComponent(g);//부모 패인트호출
 
			g.setColor(Color.BLACK);
			g.drawLine(50,250,350,250);
			
			System.out.println(podname.length);
			for(int i = 0; i < podname.length; i++){
				if(podname[i] == null || "".equals(podname[i])) continue;
				g.setColor(Color.BLACK);
				g.drawString(podname[i],i*50,200);
				
				
				g.setColor(Color.BLUE);
				g.fillRect(i*50,200,20,100);
				
				g.setColor(Color.RED);
				g.fillRect(i*50,200+(100-Math.round(memuse[i])),20, Math.round(memuse[i]));
				
				
			}
		}
		
		void setData(String[] podname, float[] memuse){
			this.podname = podname;
			this.memuse = memuse;
		}
		
	}

	
	
	/**
	 * Create the frame.
	 */
	public FrameK8STest() throws Exception{
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 1200, 800);
		this.getContentPane().setLayout(new BorderLayout(5,2));
		
		
		leftpanel.setLayout(new BoxLayout(leftpanel, BoxLayout.Y_AXIS));
		rightpanel.setLayout(new BoxLayout(rightpanel, BoxLayout.Y_AXIS));
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
		bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));
		
		textArea.setFont(new Font("Courier", Font.PLAIN, 15));
		

		
		this.getContentPane().add(new JScrollPane(leftpanel), BorderLayout.WEST);
		this.getContentPane().add(new JScrollPane(rightpanel), BorderLayout.EAST);
		this.getContentPane().add(topPanel, BorderLayout.NORTH);
		this.getContentPane().add(bottomPanel, BorderLayout.SOUTH);
		
		selectDialog = new SelectDialog(this);
//		chartPanel = new ChartPanel();
//		chartPanel.setSize(1000,350);
	
		siteLabel.setFont(new Font("", Font.BOLD, 20));
		siteLabel.setForeground(Color.BLUE);
		
		
		
	    currentModule = "po";
	    currentItem = "";
		
		topPanel.add(siteLabel, BorderLayout.NORTH);
		this.getContentPane().add(new JScrollPane(textArea), BorderLayout.CENTER);
//		this.getContentPane().add(new JScrollPane(chartPanel), BorderLayout.CENTER);
//		chartPanel.setVisible(false);
		
		
		
	    BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("site.prof")));
	   
	    siteList = new ArrayList<String>();
	    String tempinfo;
	    while ((tempinfo = br.readLine()) != null)
	    {
	      siteList.add(tempinfo);
	    }
	    String[] siteinfo;
	    
	    for (int i = 0; i < siteList.size(); i++)
	    {
	      tempinfo = (String)siteList.get(i);
	      
	      siteinfo = tempinfo.split(":");
	      
	      if(siteinfo[0].charAt(0) == '#') continue;
	      
	      
	      addButtonSite(siteinfo[0], siteinfo[1], siteinfo[2], siteinfo[3], getContentPane());

	    }
	    
	    
	    addButtonModule("po", getContentPane());
	    addButtonModule("deployment", getContentPane());
	    addButtonModule("pvc", getContentPane());
	    addButtonModule("pv", getContentPane());
	    addButtonModule("service", getContentPane());
	    addButtonModule("ingress", getContentPane());
	    
	    addButtonModule("memuse", getContentPane());
	    addButtonModule("allocCPU", getContentPane());
        
        br.close();
	    
	}

	
	//화면 하단 module 선택 버튼
	public static void addButtonModule(final String buttonName, final Container container ){
		   
		JButton button = new JButton(buttonName);
		button.setBackground(Color.WHITE);
		
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				for (int i = 0; i < bottomPanel.getComponentCount(); i++){
					bottomPanel.getComponent(i).setBackground(Color.WHITE);
		        }
		        
		        AbstractButton btn = (AbstractButton) e.getSource();
		        btn.setBackground(Color.CYAN);
				
				leftpanel.removeAll();
				rightpanel.removeAll();
				currentModule = buttonName;
				currentItem = "";
				
				ArrayList<String> cmdList = new ArrayList<String>();
				cmdList.add("kubectl get namespaces --no-headers | awk '{print $1}' ");
				ArrayList<String> result = getinfoCMD(currentHostname, currentId, currentPasswd, cmdList);
				
				textArea.setText("");
				
				for(int i = 0 ; i < result.size(); i++){
					addButtonNamespace(result.get(i), container);
					
					textArea.append(result.get(i));
				}
				
				siteLabel.setText(currentSite + " " + currentNamespace + " " + currentModule + " " + currentItem);
				container.setVisible(false);
				container.setVisible(true);
			}
		});
	    
		bottomPanel.add( button, BorderLayout.CENTER);
	}
	
	
	//Site 버튼 : 선택한 site 정보 저장 및 해당 site의 namespace 버튼 생성
	public static void addButtonSite(final String buttonName, final String hostname, final String id, final String passwd, final Container container ){
		
		JButton siteButton = new JButton(buttonName);
		siteButton.setBackground(Color.WHITE);
		
		siteButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				for (int i = 0; i < topPanel.getComponentCount(); i++){
					topPanel.getComponent(i).setBackground(Color.WHITE);
		        }
		        
		        AbstractButton btn = (AbstractButton) e.getSource();
		        btn.setBackground(Color.MAGENTA);
				
				
				leftpanel.removeAll();
				rightpanel.removeAll();
				
				currentHostname = hostname;
				currentId = id;
				currentPasswd = passwd;
				
				currentSite = buttonName;
				currentNamespace = "";
				currentItem = "";
				
				ArrayList<String> cmdList = new ArrayList<String>();
				cmdList.add("kubectl get namespaces --no-headers | awk '{print $1}' ");
				ArrayList<String> result = getinfoCMD(hostname, id, passwd, cmdList);
				
				textArea.setText("");
				
				for(int i = 0 ; i < result.size(); i++){
					addButtonNamespace(result.get(i), container);
					
					textArea.append(result.get(i));
				}
				

				siteLabel.setText(currentSite + " " + currentNamespace + " " + currentModule + " " + currentItem);
				container.setVisible(false);
				container.setVisible(true);
			}
		});
		topPanel.add( siteButton, BorderLayout.NORTH);
	}
	
	
	//namespace 버튼 : 해당 namespace에 존재하는 모듈 리스트 조회 및 describe 버튼 생성 (최초는 'po' 로 지정)
	public static void addButtonNamespace(final String buttonName, final Container container ){
		

		JButton PodButton = new JButton(buttonName);
		PodButton.setBackground(Color.WHITE);
		
		PodButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				rightpanel.removeAll();
				currentNamespace = buttonName;
				currentItem = "";
				
				
				for (int i = 0; i < leftpanel.getComponentCount(); i++){
					leftpanel.getComponent(i).setBackground(Color.WHITE);
		        }
		        
		        AbstractButton btn = (AbstractButton) e.getSource();
		        btn.setBackground(Color.ORANGE);
		        
				
				ArrayList<String> cmdList = new ArrayList<String>();
				ArrayList<String> cmdListButton = new ArrayList<String>();
				ArrayList<String> result;
				ArrayList<String> resultbutton;
				
				String addOption = "";
				if ("memuse".equals(currentModule)){
					
					
					cmdList.add("/home/jenkins/jdal/usage_mem_namesp.sh "+ buttonName);
					result = getinfoCMD(currentHostname, currentId, currentPasswd, cmdList);
					
					textArea.setText("");
					
					String tempStr = "";
					StringTokenizer st;
					String podname[] = new String[result.size()];
					double memuse[] = new double[result.size()];
					DefaultCategoryDataset barDataset = new DefaultCategoryDataset(); // chart dataset 
					
					for(int i = 0 ; i < result.size(); i++){
						
						tempStr = result.get(i);
						textArea.append(tempStr + " \n");
						if( tempStr.indexOf(buttonName) != -1 ){
							st = new StringTokenizer(tempStr);
							
							st.nextToken();
							podname[i] = st.nextToken();
							st.nextToken();
							st.nextToken();
							memuse[i] = Float.parseFloat(st.nextToken());
							
							System.out.println(podname[i] + " " + memuse[i]);
							
							barDataset.setValue(memuse[i], "POD", podname[i]);
						}
					}
					
					//Create the chart
					JFreeChart chart = ChartFactory.createBarChart(
					        "POD Memory USE Chart", "POD", "MEM USE", barDataset,
					        PlotOrientation.HORIZONTAL, false, true, false);
					
						
				    //Render the frame
				    ChartFrame chartFrame = new ChartFrame("POD Memory USE Chart", chart);
				    
				    chartFrame.setVisible(true);
				    chartFrame.setSize(560, 350);
				    
//					chartPanel.setData(podname, memuse);
//					chartPanel.repaint();
//					chartPanel.setVisible(true);
					
					
				} else if("allocCPU".equals(currentModule)){
					cmdList.add("/home/jenkins/jdal/alloc_cpu.sh");
					result = getinfoCMDOneLine(currentHostname, currentId, currentPasswd, cmdList);
					
					textArea.setText("");
					
					for(int i = 0 ; i < result.size(); i++){
						textArea.append(result.get(i));
					}
				}
				else{ 
					if("pv".equals(currentModule)){
						addOption = " | grep " + currentNamespace;
					}
					
					cmdList.add("kubectl get "+currentModule+" -n "+ buttonName + addOption);
					cmdListButton.add("kubectl get "+currentModule+" -n " + buttonName  +  " --no-headers " + addOption +" | awk '{print $1}'" );
					
					result = getinfoCMDOneLine(currentHostname, currentId, currentPasswd, cmdList);
					resultbutton = getinfoCMD(currentHostname, currentId, currentPasswd, cmdListButton);
					
					textArea.setText("");
					
					for(int i = 0 ; i < result.size(); i++){
						textArea.append(result.get(i));
					}
					
					for(int i = 0 ; i < resultbutton.size(); i++){
						addButtonModuleInfo(buttonName, resultbutton.get(i), container);
					}
					
					if (result.size() ==0 ){
						textArea.append("Not Found");
					}
				}
				
				
				
				siteLabel.setText(currentSite + " " + currentNamespace + " " + currentModule + " " + currentItem);
				
				container.setVisible(false);
				container.setVisible(true);
			}
		});
		leftpanel.add( PodButton, BorderLayout.CENTER);
	}
	
	//describe 버튼 : 상세 정보 가운데 textarea에 출력
//	public static void addButtonModuleInfo(final String namespaces, final String buttonName, final Container container ){
//		
//
//		JButton PodButton = new JButton(buttonName);
//		
//		PodButton.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				
//				ArrayList<String> cmdList = new ArrayList<String>();
//				
//				if( "po".equals(currentModule)){
//					String[] options = {"Describe", "JEUS Option(Domain.xml)"}; 
//		            int resultD = JOptionPane.showOptionDialog(
//		            	container,
//		               "Select Option", 
//		               "Select Option",            
//		               JOptionPane.YES_NO_OPTION,
//		               JOptionPane.QUESTION_MESSAGE,
//		               null,     //no custom icon
//		               options,  //button titles
//		               options[0] //default button
//		            );
//		            if(resultD == JOptionPane.YES_OPTION){
//		            	cmdList.add("kubectl describe "+currentModule+ " -n "+ namespaces + " " + buttonName );
//		            }else {
//		            	cmdList.add("kubectl exec -n "+ namespaces + " " + buttonName + " -- cat /home/tmax/jeus8/domains/jeus_domain/config/domain.xml");
//		            }
//				} else{
//					cmdList.add("kubectl describe "+currentModule+ " -n "+ namespaces + " " + buttonName );
//				}
//			
//								
//				ArrayList<String> result = getinfoCMDOneLine(currentHostname, currentId, currentPasswd, cmdList);
//								
//				textArea.setText("");
//				
//				for(int i = 0 ; i < result.size(); i++){
//					textArea.append(result.get(i));
//				}
//				
//		
//			}
//		});
//		rightpanel.add(PodButton, BorderLayout.CENTER);
//	}
	
	public static void addButtonModuleInfo(final String namespaces, final String buttonName, final Container container ){
		

		JButton PodButton = new JButton(buttonName);
		PodButton.setBackground(Color.WHITE);
		
		PodButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				for (int i = 0; i < rightpanel.getComponentCount(); i++){
		        	rightpanel.getComponent(i).setBackground(Color.WHITE);
		        }
		        
		        AbstractButton btn = (AbstractButton) e.getSource();
		        btn.setBackground(Color.green);
		        
		        
				currentItem = "[" +buttonName + "]";
				
				ArrayList<String> cmdList = new ArrayList<String>();
				
				if( "po".equals(currentModule)){
					selectDialog.setBounds(selectDialog.getParent().getBounds().x+ 100, selectDialog.getParent().getBounds().y+ 100, 300,200);
					selectDialog.setVisible(true);
					
					if ("describe".equals(selectDialog.select)){
						cmdList.add("kubectl describe "+currentModule+ " -n "+ namespaces + " " + buttonName );
					} else if("jeusoption".equals(selectDialog.select)){
						cmdList.add("kubectl exec -n "+ namespaces + " " + buttonName + " -- cat /home/tmax/jeus8/domains/jeus_domain/config/domain.xml");
					}else{
						cmdList.add("kubectl exec -n "+ namespaces + " " + buttonName + " -- tail -5000 /logs/"+buttonName+"/JeusServer.log");
					}
				} else{
					cmdList.add("kubectl describe "+currentModule+ " -n "+ namespaces + " " + buttonName );
				}
			
								
				ArrayList<String> result = getinfoCMDOneLine(currentHostname, currentId, currentPasswd, cmdList);
								
				textArea.setText("");
				
				for(int i = 0 ; i < result.size(); i++){
					textArea.append(result.get(i));
				}
				
				siteLabel.setText(currentSite + " " + currentNamespace + " " + currentModule + " " + currentItem);
				
		
			}
		});
		rightpanel.add(PodButton, BorderLayout.CENTER);
	}
	 	
	
	 public static void connect(String host, int port, String user, String password)
	 {
	   try
	   {
	     System.out.println("connecting..." + host);
	     
	     JSch jsch = new JSch();
	     
	     session = jsch.getSession(user, host, port);
	     
	     session.setConfig("StrictHostKeyChecking", "no");
	     
	     session.setPassword(password);
	     
	     session.connect();
	   }
	   catch (Exception e)
	   {
	     e.printStackTrace();
	   }
	 }
	 
	 public static void disconnect()
	 {
	   if (channel != null) {
	     channel.disconnect();
	   }
	   if (channelShell != null){
		   channelShell.disconnect();
	   }
	   if (session != null) {
	     session.disconnect();
	   }
	 }
	 
	 public static ArrayList<String> cmd(String host, String cmd)
	 {
	   ArrayList<String> result = new ArrayList<String>();
       BufferedReader reader = null;
         
	   try
	   {
	     channel = session.openChannel("exec");
	     
	     sftpChannelExec = (ChannelExec)channel;
	     
	     InputStream in = sftpChannelExec.getInputStream();
	     
	     System.out.println("==> Connected to " + host + " cmd [" + cmd + "]");
	     sftpChannelExec.setCommand(cmd);
	     sftpChannelExec.connect();
	     
	     
//	     result.add("==> Connected to " + host + " cmd [" + cmd + "]");
//	     result.add(" ");
	     reader = new BufferedReader(new InputStreamReader(in));
	     String line;
	     while ((line = reader.readLine()) != null)
	     {
	       result.add(line);
	       //System.out.println(line);
	     }
	   }
	   catch (Exception e)
	   {
	     e.printStackTrace();
	   } finally{
           try {
               if(reader != null) reader.close();
           } catch(IOException e){
               e.printStackTrace();
           }
       }
	   return result;
	 }
	 
	 public static ArrayList<String> cmdOneLine(String host, String cmd)
	 {
	   ArrayList<String> result = new ArrayList<String>();
       BufferedReader reader = null;
	   
       try{
           
	     channel = session.openChannel("exec");
	     
	     sftpChannelExec = (ChannelExec)channel;
	     
	     InputStream in = sftpChannelExec.getInputStream();
	     
	     System.out.println("==> Connected to " + host + " cmd [" + cmd + "]");
	     sftpChannelExec.setCommand(cmd);
	     sftpChannelExec.connect();
	     
	     reader = new BufferedReader(new InputStreamReader(in));
	     String line;
	     String tempStr ="";
	     while ((line = reader.readLine()) != null)
	     {
	   	tempStr += (line +"\n");
	     }
	     if( !"".equals(tempStr)){
	   	  result.add("==> [" + cmd + "] \n");
	   	  result.add(tempStr);
	     }
	     //System.out.println(tempStr);
	   }
	   catch (Exception e)
	   {
	     e.printStackTrace();
	   }finally{
           try {
               if(reader != null) reader.close();
           } catch(IOException e){
               e.printStackTrace();
           }
       }
	   return result;
	 }
		 
	  public static ArrayList<String> getinfoCMDOneLine(String hostip, String user, String passwd, ArrayList<String> cmdList)
	  {
	    ArrayList<String> list = new ArrayList<String>();
	    try
	    {
	      connect(hostip, 22, user, passwd);
	      for (int a = 0; a < cmdList.size(); a++)
	      {
	        ArrayList<String> templist = cmdOneLine(hostip, (String)cmdList.get(a));
	        
	        for (int i = 0; i < templist.size(); i++) {
	          list.add((String)templist.get(i));
	        }
	      }
	      disconnect();
	    }
	    catch (Exception e)
	    {
	      e.printStackTrace();
	    }
	    return list;
	  }
	  
	  
	  public static ArrayList<String> getinfoCMD(String hostip, String user, String passwd, ArrayList<String> cmdList)
	  {
	    ArrayList<String> list = new ArrayList<String>();
	    try
	    {
	      connect(hostip, 22, user, passwd);
	      for (int a = 0; a < cmdList.size(); a++)
	      {
	        ArrayList<String> templist = cmd(hostip, (String)cmdList.get(a));
	        
	        for (int i = 0; i < templist.size(); i++) {
	          list.add((String)templist.get(i));
	        }
	      }
	      disconnect();
	    }
	    catch (Exception e)
	    {
	      e.printStackTrace();
	    }
	    return list;
	  }
	  
	  
	  public static ArrayList<String> filedownload(String host, String downloadpath, String uploadfile, String filename)
	  {
	    ArrayList<String> result = new ArrayList<String>();
	    try
	    {
	      channel = session.openChannel("sftp");
	      channelSftp = (ChannelSftp)channel;
	      channelSftp.connect();
	      
	      channelSftp.get(downloadpath + filename);
	      
	      channelSftp.disconnect();
	    }
	    catch (Exception e)
	    {
	      e.printStackTrace();
	    }
	    System.out.println("[" + filename + "] complete");
	    return result;
	  }
}

