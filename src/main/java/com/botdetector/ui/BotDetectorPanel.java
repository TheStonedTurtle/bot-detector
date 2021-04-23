package com.botdetector.ui;

import com.botdetector.BotDetectorPlugin;
import com.botdetector.http.BotDetectorClient;
import com.botdetector.model.PlayerStats;
import com.google.common.collect.ImmutableList;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.runelite.api.Client;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.IconTextField;
import net.runelite.client.util.ColorUtil;
import net.runelite.client.util.LinkBrowser;
import net.runelite.client.util.Text;

public class BotDetectorPanel extends PluginPanel
{
	@Getter
	@AllArgsConstructor
	private static enum WebLink
	{
		WEBSITE(Icons.WEB_ICON, "Our website", "https://www.osrsbotdetector.com/"),
		DISCORD(Icons.DISCORD_ICON, "Join our Discord!", "https://discord.com/invite/JCAGpcjbfP"),
		GITHUB(Icons.GITHUB_ICON, "Check out the project's source code", "https://github.com/Bot-detector"),
		PATREON(Icons.PATREON_ICON, "Help keep us going!", "https://www.patreon.com/bot_detector")
		;

		private final ImageIcon image;
		private final String tooltip;
		private final String link;
	}

	private static final int MAX_RSN_LENGTH = 12;
	private static final Font BOLD_FONT = FontManager.getRunescapeBoldFont();
	private static final Font NORMAL_FONT = FontManager.getRunescapeFont();
	private static final Font SMALL_FONT = FontManager.getRunescapeSmallFont();

	private static final Color BACKGROUND_COLOR = ColorScheme.DARK_GRAY_COLOR;
	private static final Color SUB_BACKGROUND_COLOR = ColorScheme.DARKER_GRAY_COLOR;
	private static final Color LINK_HEADER_COLOR = ColorScheme.LIGHT_GRAY_COLOR;
	private static final Color HEADER_COLOR = Color.WHITE;
	private static final Color TEXT_COLOR = ColorScheme.LIGHT_GRAY_COLOR;

	private static final List<WebLink> LINKS = ImmutableList.of(
		WebLink.WEBSITE,
		WebLink.DISCORD,
		WebLink.GITHUB,
		WebLink.PATREON);

	private final IconTextField searchBar;
	private final JPanel linksPanel;
	private final JPanel reportingStatsPanel;
	private final JPanel primaryPredictionPanel;
	private final JPanel predictionBreakdownPanel;

	private final Client client;
	private final BotDetectorPlugin plugin;
	private final BotDetectorClient detectorClient;

	private boolean searchBarLoading;

	// Player Stats
	private JLabel playerStatsUploadedNamesLabel;
	private JLabel playerStatsReportsLabel;
	private JLabel playerStatsPossibleBansLabel;
	private JLabel playerStatsConfirmedBansLabel;
	private JLabel playerStatsAnonymousWarningLabel;

	// Prediction Breakdown
	private JLabel predictionBreakdownLabel;

	@Inject
	public BotDetectorPanel(@Nullable Client client, BotDetectorPlugin plugin, BotDetectorClient detectorClient)
	{
		this.client = client;
		this.plugin = plugin;
		this.detectorClient = detectorClient;

		setBorder(new EmptyBorder(18, 10, 0, 10));
		setBackground(BACKGROUND_COLOR);
		setLayout(new GridBagLayout());

		searchBar = playerSearchBar();
		linksPanel = linksPanel();
		reportingStatsPanel = reportingStatsPanel();
		primaryPredictionPanel = primaryPredictionPanel();
		predictionBreakdownPanel = predictionBreakdownPanel();

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1;
		c.weighty = 0;
		c.insets = new Insets(0, 0, 10, 0);

		add(linksPanel, c);
		c.gridy++;
		add(reportingStatsPanel, c);
		c.gridy++;
		add(searchBar, c);
		c.gridy++;
		add(primaryPredictionPanel, c);
		c.gridy++;
		add(predictionBreakdownPanel, c);
		c.gridy++;
	}

	private JPanel linksPanel()
	{
		JPanel linksPanel = new JPanel();
		linksPanel.setBorder(new EmptyBorder(0, 6, 0, 0));
		linksPanel.setBackground(SUB_BACKGROUND_COLOR);

		JLabel title = new JLabel("Connect With Us: ");
		title.setForeground(LINK_HEADER_COLOR);
		title.setFont(NORMAL_FONT);

		linksPanel.add(title);

		for (WebLink w : LINKS)
		{
			JLabel link = new JLabel(w.getImage());
			link.setToolTipText(w.getTooltip());
			link.addMouseListener(new MouseAdapter()
			{
				@Override
				public void mousePressed(MouseEvent e)
				{
					LinkBrowser.browse(w.getLink());
				}
			});

			linksPanel.add(link);
		}

		return linksPanel;
	}

	private JPanel reportingStatsPanel()
	{
		JLabel label;

		JPanel reportingStatsPanel = new JPanel();
		reportingStatsPanel.setBackground(SUB_BACKGROUND_COLOR);
		reportingStatsPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

		reportingStatsPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;

		label = new JLabel("Reporting Statistics");
		label.setFont(BOLD_FONT);
		label.setForeground(HEADER_COLOR);

		c.gridx = 0;
		c.gridy = 0;
		c.ipady = 5;
		c.gridwidth = 2;
		c.weightx = 1;
		reportingStatsPanel.add(label, c);

		label = new JLabel("Names Uploaded: ");
		label.setToolTipText("How many names uploaded during the current session.");
		label.setFont(SMALL_FONT);
		label.setForeground(TEXT_COLOR);

		c.gridy = 1;
		c.gridy++;
		c.ipady = 3;
		c.gridwidth = 1;
		c.weightx = 0;
		reportingStatsPanel.add(label, c);

		playerStatsUploadedNamesLabel = new JLabel();
		playerStatsUploadedNamesLabel.setFont(SMALL_FONT);
		playerStatsUploadedNamesLabel.setForeground(TEXT_COLOR);
		label.setFont(NORMAL_FONT);
		label.setForeground(TEXT_COLOR);
		c.gridx = 1;
		c.weightx = 1;
		reportingStatsPanel.add(playerStatsUploadedNamesLabel, c);

		label = new JLabel("Reports Made: ");
		label.setToolTipText("How many names/locations you've sent to us.");
		label.setFont(SMALL_FONT);
		label.setForeground(TEXT_COLOR);
		c.gridy++;
		c.gridx = 0;
		c.weightx = 0;
		reportingStatsPanel.add(label, c);

		playerStatsReportsLabel = new JLabel();
		playerStatsReportsLabel.setFont(SMALL_FONT);
		playerStatsReportsLabel.setForeground(TEXT_COLOR);
		c.gridx = 1;
		c.weightx = 1;
		reportingStatsPanel.add(playerStatsReportsLabel, c);

		label = new JLabel("Confirmed Bans: ");
		label.setToolTipText("How many of your reported names lead to confirmed bans by Jagex.");
		label.setFont(SMALL_FONT);
		label.setForeground(TEXT_COLOR);
		c.gridy++;
		c.gridx = 0;
		c.weightx = 0;
		reportingStatsPanel.add(label, c);

		playerStatsConfirmedBansLabel = new JLabel();
		playerStatsConfirmedBansLabel.setFont(SMALL_FONT);
		playerStatsConfirmedBansLabel.setForeground(TEXT_COLOR);
		c.gridx = 1;
		c.weightx = 1;
		reportingStatsPanel.add(playerStatsConfirmedBansLabel, c);

		label = new JLabel("Probable Bans: ");
		label.setToolTipText("How many of your reported names may have been banned (e.g. Names that no longer appear on the Hiscores).");
		label.setFont(SMALL_FONT);
		label.setForeground(TEXT_COLOR);
		c.gridy++;
		c.gridx = 0;
		c.weightx = 0;
		reportingStatsPanel.add(label, c);

		playerStatsPossibleBansLabel = new JLabel();
		playerStatsPossibleBansLabel.setFont(SMALL_FONT);
		playerStatsPossibleBansLabel.setForeground(TEXT_COLOR);
		c.gridx = 1;
		c.weightx = 1;
		reportingStatsPanel.add(playerStatsPossibleBansLabel, c);

		playerStatsAnonymousWarningLabel = new JLabel(" Anonymous Reporting Active");
		playerStatsAnonymousWarningLabel.setIcon(Icons.WARNING_ICON);
		playerStatsAnonymousWarningLabel.setToolTipText("Your reports will not be added to your tallies.");
		c.gridy++;
		c.gridx = 0;
		c.weightx = 1;
		c.gridwidth = 2;
		c.ipady = 5;
		reportingStatsPanel.add(playerStatsAnonymousWarningLabel, c);

		return reportingStatsPanel;
	}

	private IconTextField playerSearchBar()
	{
		IconTextField searchBar = new IconTextField();
		searchBar.setIcon(IconTextField.Icon.SEARCH);
		searchBar.setPreferredSize(new Dimension(PluginPanel.PANEL_WIDTH - 20, 30));
		searchBar.setBackground(SUB_BACKGROUND_COLOR);
		searchBar.setHoverBackgroundColor(ColorScheme.DARK_GRAY_HOVER_COLOR);
		searchBar.setMinimumSize(new Dimension(0, 30));
		searchBar.addActionListener(e -> detectPlayer());
		searchBar.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				if (e.getClickCount() != 2)
				{
					return;
				}

				String name = plugin.getLoggedPlayerName();
				if (name != null)
				{
					detectPlayer(name);
				}
			}
		});
		searchBar.addClearListener(() ->
		{
			searchBar.setIcon(IconTextField.Icon.SEARCH);
			searchBar.setEditable(true);
			searchBarLoading = false;
		});

		return searchBar;
	}

	private JPanel primaryPredictionPanel()
	{
		JLabel label;

		JPanel primaryPredictionPanel = new JPanel();
		primaryPredictionPanel.setBackground(SUB_BACKGROUND_COLOR);
		primaryPredictionPanel.setLayout(new GridBagLayout());
		primaryPredictionPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;

		label = new JLabel("Primary Prediction");
		label.setFont(BOLD_FONT);
		label.setForeground(HEADER_COLOR);
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = .5;
		c.weighty = 1;
		c.ipady = 5;
		primaryPredictionPanel.add(label, c);

		label = new JLabel("Player Name:");
		label.setFont(SMALL_FONT);
		label.setForeground(TEXT_COLOR);
		c.gridy++;
		c.ipady = 3;
		primaryPredictionPanel.add(label, c);

		label = new JLabel("Prediction:");
		label.setFont(SMALL_FONT);
		label.setForeground(TEXT_COLOR);
		c.gridy++;
		primaryPredictionPanel.add(label, c);

		label = new JLabel("Confidence:");
		label.setFont(SMALL_FONT);
		label.setForeground(TEXT_COLOR);
		c.gridy++;
		primaryPredictionPanel.add(label, c);

		return primaryPredictionPanel;
	}

	private JPanel predictionBreakdownPanel()
	{
		JPanel predictionBreakdownPanel = new JPanel();
		predictionBreakdownPanel.setBackground(SUB_BACKGROUND_COLOR);
		predictionBreakdownPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		predictionBreakdownPanel.setLayout(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;

		JLabel label = new JLabel("Prediction Breakdown");
		label.setFont(BOLD_FONT);
		label.setForeground(HEADER_COLOR);
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1.0;
		c.ipady = 5;
		predictionBreakdownPanel.add(label, c);

		predictionBreakdownLabel = new JLabel();
		predictionBreakdownLabel.setFont(SMALL_FONT);
		predictionBreakdownLabel.setForeground(TEXT_COLOR);
		c.anchor = GridBagConstraints.PAGE_END;
		c.gridy++;
		predictionBreakdownPanel.add(predictionBreakdownLabel, c);

		return predictionBreakdownPanel;
	}

	public void setNamesUploaded(int num)
	{
		playerStatsUploadedNamesLabel.setText(String.valueOf(num));
	}

	public void setPlayerStats(PlayerStats ps)
	{
		if (ps != null)
		{
			playerStatsReportsLabel.setText(String.valueOf(ps.getReports()));
			playerStatsConfirmedBansLabel.setText(String.valueOf(ps.getBans()));
			playerStatsPossibleBansLabel.setText(String.valueOf(ps.getPossibleBans()));
		}
		else
		{
			playerStatsReportsLabel.setText("");
			playerStatsConfirmedBansLabel.setText("");
			playerStatsPossibleBansLabel.setText("");
		}
	}

	public void setAnonymousWarning(boolean warn)
	{
		playerStatsAnonymousWarningLabel.setVisible(warn);
	}

	public void detectPlayer(String rsn)
	{
		searchBar.setText(rsn);
		detectPlayer();
	}

	private void detectPlayer()
	{
		String sanitizedRSN = Text.sanitize(searchBar.getText());

		if (sanitizedRSN.length() <= 0)
		{
			return;
		}

		if (sanitizedRSN.length() > MAX_RSN_LENGTH)
		{
			searchBar.setIcon(IconTextField.Icon.ERROR);
			searchBarLoading = false;
			return;
		}

		searchBar.setIcon(IconTextField.Icon.LOADING_DARKER);
		searchBar.setEditable(false);
		searchBarLoading = true;

		//TODO Trigger prediction lookup here
	}

	private Color getPredictionColor(double prediction)
	{
		prediction = Math.min(Math.max(0.0, prediction), 1.0);
		if (prediction < 0.5)
		{
			return ColorUtil.colorLerp(Color.RED, Color.YELLOW, prediction * 2);
		}
		else
		{
			return ColorUtil.colorLerp(Color.YELLOW, Color.GREEN, (prediction - 0.5) * 2);
		}
	}

	private String getPredictionBreakdownString(Map<String, Double> predictionMap)
	{
		if (predictionMap == null || predictionMap.size() == 0)
		{
			return null;
		}

		String openingTags = "<html><body style = 'color:" + ColorUtil.toHexColor(TEXT_COLOR) + "'>";
		String closingTags = "</html><body>";

		StringBuilder sb = new StringBuilder();
		sb.append(openingTags);

		predictionMap.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
			.forEach(e ->
				sb.append("<p>").append(normalizeLabel(e.getKey()))
				.append(": <span style = 'color:").append(ColorUtil.toHexColor(getPredictionColor(e.getValue())))
				.append("'>").append(getPercentString(e.getValue())).append("</span></p>"));

		return sb.append(closingTags).toString();
	}

	private String normalizeLabel(String label)
	{
		return label.replace("_", " ").trim();
	}

	private String getPercentString(double percent)
	{
		return String.format("%.2f%%", percent * 100);
	}
}