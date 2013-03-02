/**
 * Warlock, the open-source cross-platform game client
 *  
 * Copyright 2008, Warlock LLC, and individual contributors as indicated
 * by the @authors tag. 
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package cc.warlock.rcp.application;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.operations.ProvisioningSession;
import org.eclipse.equinox.p2.operations.Update;
import org.eclipse.equinox.p2.operations.UpdateOperation;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import cc.warlock.rcp.plugin.Warlock2Plugin;

public class WarlockUpdates {

	public static List<Update> promptUpgrade (List<Update> updates)
	{
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		WarlockUpdateDialog dialog = new WarlockUpdateDialog(shell, updates);
		int response = dialog.open();
		
		if (response == Window.OK)
		{
			return dialog.getSelectedUpdates();
		}
		
		return Collections.emptyList();
	}
	
	
	public static final String UPDATE_SITE = "warlock.updates.url";
	public static final String AUTO_UPDATE = "warlock.updates.autoupdate";
	
	protected static Properties updateProperties;
	protected static Properties getUpdateProperties ()
	{
		if (updateProperties == null)
		{
			updateProperties = new Properties();
			try {
				InputStream stream = FileLocator.openStream(Warlock2Plugin.getDefault().getBundle(), new Path("warlock-updates.properties"), false);
				updateProperties.load(stream);
				stream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return updateProperties;
	}
	
	public static boolean autoUpdate ()
	{
		boolean autoUpdate = false;
		Properties updateProperties = getUpdateProperties();
		
		if (updateProperties.containsKey(AUTO_UPDATE))
		{
			autoUpdate = Boolean.parseBoolean(updateProperties.getProperty(AUTO_UPDATE));
		}
		return autoUpdate;
	}
	
	public static void checkForUpdates (final IProgressMonitor monitor)
	{
		//try {
			Properties properties = getUpdateProperties();
			String url = properties.getProperty(UPDATE_SITE);
			if (url == null)
				return;

			BundleContext context = FrameworkUtil.getBundle(WarlockUpdates.class).getBundleContext();
		    ServiceReference<?> reference = context
	                .getServiceReference(IProvisioningAgent.SERVICE_NAME);
	        if (reference == null)
	            return;
	        Object obj = context.getService(reference);
	        IProvisioningAgent agent = (IProvisioningAgent) obj;
	        ProvisioningSession session = new ProvisioningSession(agent);
	        UpdateOperation update = new UpdateOperation(session);
	        Update[] updates = update.getPossibleUpdates();
	        IStatus result = update.resolveModal(monitor);
	        if (result.isOK()) {
	            update.getProvisioningJob(monitor).schedule();
	        } else {
	            // can't update for some reason
	        }
	        context.ungetService(reference);
/*
			ISite updateSite = SiteManager.getSite(new URL(url), monitor);
			IFeatureReference[] featureRefs = updateSite.getFeatureReferences();
			final ILocalSite localSite = SiteManager.getLocalSite();
			final IConfiguredSite configuredSite = localSite.getCurrentConfiguration().getConfiguredSites()[0];
			IFeatureReference[] localFeatureRefs = configuredSite.getConfiguredFeatures();
			
			final HashMap<IFeatureReference, VersionedIdentifier> newVersions  = new HashMap<IFeatureReference, VersionedIdentifier>();

			for (int i = 0; i < featureRefs.length; i++) {
				for (int j = 0; j < localFeatureRefs.length; j++) {

					VersionedIdentifier featureVersion = featureRefs[i].getVersionedIdentifier();
					VersionedIdentifier localFeatureVersion = localFeatureRefs[j].getVersionedIdentifier();

					if (featureVersion.getIdentifier().equals(localFeatureVersion.getIdentifier())) {

						if (featureVersion.getVersion().isGreaterThan(localFeatureVersion.getVersion())) {

							newVersions.put(featureRefs[i], localFeatureVersion);
						}
					}
				}
			}

			if (newVersions.size() > 0)
			{
				Display.getDefault().syncExec(new Runnable() {
					public void run () {
						final List<IFeatureReference> featuresToUpgrade = promptUpgrade(newVersions);
						
						ProgressMonitorDialog dialog = new ProgressMonitorDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
						dialog.setBlockOnOpen(false);
						dialog.open();
						try {
							dialog.run(true, true, new IRunnableWithProgress () {
								public void run(IProgressMonitor monitor)
										throws InvocationTargetException,
										InterruptedException {
									try {
										for (IFeatureReference featureRef : featuresToUpgrade)
										{
											IFeature feature = featureRef.getFeature(monitor);
											
											IInstallFeatureOperation operation = OperationsManager.getOperationFactory().createInstallOperation(
												configuredSite, feature, null, null, null);
											
											operation.execute(monitor, null);
										}
										if (featuresToUpgrade.size() > 0) {
											localSite.save();
											
											IFeatureReference featureRef = featuresToUpgrade.get(0);
											IFeature feature = featureRef.getFeature(monitor);
											// Force a restart. Because everyone reports bugs before restarting.
											PlatformUI.getWorkbench().restart();
										}
									} catch (CoreException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									} catch (InvocationTargetException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
							});
						} catch (InvocationTargetException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				});
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CoreException e) {
			MessageDialog.openError(Display.getDefault().getActiveShell(), "Error updating",
					"There was an error while attempting to update Warlock: " + e.getMessage());
		}*/
	}
}
