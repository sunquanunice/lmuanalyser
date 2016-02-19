package lmuanalyser;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.PlatformUI;

public class JavaProjectDependenceHandler extends AbstractHandler{

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getSelection();
		if (!(selection instanceof StructuredSelection))
			return null;
		Object selected = ((StructuredSelection) selection).getFirstElement();
		IResource resource = null;
		String path = "";
		if (selected instanceof IProject) {
			resource = (IResource) Platform.getAdapterManager().getAdapter(selected, IResource.class);
			path = resource.getLocation().toString();
			/*
			try {
				InputStream input = new FileInputStream(new File(path + "/META-INF/MANIFEST.MF"));
				
				 Manifest manifest = new Manifest(input);
				 //ignore the bundle version and visibility
				 String attrs = manifest.getMainAttributes().getValue("Require-Bundle").replaceAll(";bundle-version=\"[^\"]*\"", "").replaceAll(";visibility:=\\w+", "");
				 for(String s : attrs.split(",")) {
					 System.out.println(s);
				 }
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("**********************");
			System.out.println();
			File fXmlFile = new File("plugin.xml");
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			
			try {
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				Document doc  = dBuilder.parse(fXmlFile);	
				//optional, but recommended
				//read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
				doc.getDocumentElement().normalize();
				NodeList nList = doc.getElementsByTagName("extension");
				for (int temp = 0; temp < nList.getLength(); temp++) {
					Node nNode = nList.item(temp);
					if (nNode.getNodeType() == Node.ELEMENT_NODE) {
						Element eElement = (Element) nNode;
						System.out.println(eElement.getAttribute("point"));
					}
				}
				} catch (SAXException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
			new ResultGenerator().parser("loadProject " + path);
			/*for(IExtensionPoint point : Platform.getExtensionRegistry().getExtensionPoints()) {
				System.out.println(point.getNamespaceIdentifier() + "  " + point.getUniqueIdentifier());
			}*/
		} else if (selected instanceof PlatformObject) {
			IProject project = (IProject) Platform.getAdapterManager().getAdapter(selected, IProject.class);
			resource = (IResource) Platform.getAdapterManager().getAdapter(project, IResource.class);
			path = resource.getLocation().toString();
			new ResultGenerator().parser("loadProject " + path);
			/*try {
				InputStream input = new FileInputStream(new File(path + "/META-INF/MANIFEST.MF"));
				
				 Manifest manifest = new Manifest(input);
				 //ignore the bundle version and visibility
				 String attrs = manifest.getMainAttributes().getValue("Require-Bundle").replaceAll(";bundle-version=\"[^\"]*\"", "").replaceAll(";visibility:=\\w+", "");
				 for(String s : attrs.split(",")) {
					 System.out.println(s);
				 }
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("**********************");
			System.out.println();
			/*for(IExtensionPoint point : Platform.getExtensionRegistry().getExtensionPoints()) {
				System.out.println(point + "   " + point.getContributor().getName());
			}
			File fXmlFile = new File(path + "/plugin.xml");
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			
			try {
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				Document doc  = dBuilder.parse(fXmlFile);	
				//optional, but recommended
				//read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
				doc.getDocumentElement().normalize();
				NodeList nList = doc.getElementsByTagName("extension");
				for (int temp = 0; temp < nList.getLength(); temp++) {
					Node nNode = nList.item(temp);
					if (nNode.getNodeType() == Node.ELEMENT_NODE) {
						Element eElement = (Element) nNode;
						System.out.println(eElement.getAttribute("point"));
					}
				}
				} catch (SAXException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
		}
		return null;
	}

}
