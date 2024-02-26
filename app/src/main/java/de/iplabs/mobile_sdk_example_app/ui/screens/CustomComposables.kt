package de.iplabs.mobile_sdk_example_app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import de.iplabs.mobile_sdk_example_app.R

@Composable
fun ExpandableCard(
	id: Int,
	title: @Composable () -> Unit,
	expandableContent: @Composable () -> Unit,
	expandedItemId: Int,
	onExpand: (Int) -> Unit,
	onCollapse: (Int) -> Unit
) {
	val expanded = (expandedItemId == id)
	val cardShape = MaterialTheme.shapes.medium

	Card(
		modifier = Modifier
			.fillMaxWidth()
			.clip(cardShape),
		shape = cardShape,
		colors = CardDefaults.cardColors(
			containerColor = MaterialTheme.colorScheme.surfaceVariant,
			contentColor = MaterialTheme.colorScheme.onSurfaceVariant
		)
	) {
		Column {
			Row(
				modifier = Modifier
					.clickable { if (expanded) onCollapse(id) else onExpand(id) }
					.padding(start = 12.dp, top = 8.dp, end = 8.dp, bottom = 8.dp),
				horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
			) {
				Row(modifier = Modifier.weight(weight = 1f, fill = true)) {
					title()
				}

				Icon(
					imageVector = if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
					contentDescription = null,
					modifier = Modifier
						.size(size = 32.dp)
				)
			}

			if (expanded) {
				expandableContent()
			}
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlusMinusCounter(
	count: Int = 1,
	onDecrease: () -> Unit,
	onIncrease: () -> Unit,
	cornerRoundingPercentage: Int = 50
) {
	require(cornerRoundingPercentage in 0..50) {
		"cornerRoundingPercentage must be in between 0 and 50."
	}

	Row(
		modifier = Modifier
			.clip(shape = RoundedCornerShape(percent = cornerRoundingPercentage))
			.background(color = MaterialTheme.colorScheme.background),
		verticalAlignment = Alignment.CenterVertically
	) {
		TooltipBox(
			positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
			tooltip = {
				PlainTooltip { Text(text = stringResource(id = R.string.decrease_count)) }
			},
			state = rememberTooltipState()
		) {
			FilledIconButton(
				onClick = { onDecrease() },
				modifier = Modifier
					.size(size = 32.dp),
				enabled = count > 1,
				shape = RoundedCornerShape(
					topStartPercent = cornerRoundingPercentage,
					topEndPercent = 0,
					bottomEndPercent = 0,
					bottomStartPercent = cornerRoundingPercentage
				)
			) {
				Icon(
					imageVector = Icons.Outlined.Remove,
					contentDescription = null,
					modifier = Modifier.size(size = 24.dp)
				)
			}
		}

		Text(
			text = count.toString(),
			modifier = Modifier
				.padding(horizontal = 6.dp)
				.defaultMinSize(minWidth = 20.dp)
				.background(color = MaterialTheme.colorScheme.background),
			textAlign = TextAlign.Center,
			maxLines = 1,
			style = MaterialTheme.typography.bodyMedium
		)

		TooltipBox(
			positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
			tooltip = {
				PlainTooltip { Text(text = stringResource(id = R.string.increase_count)) }
			},
			state = rememberTooltipState()
		) {
			FilledIconButton(
				onClick = { onIncrease() },
				modifier = Modifier
					.size(size = 32.dp),
				shape = RoundedCornerShape(
					topStartPercent = 0,
					topEndPercent = cornerRoundingPercentage,
					bottomEndPercent = cornerRoundingPercentage,
					bottomStartPercent = 0
				)
			) {
				Icon(
					imageVector = Icons.Outlined.Add,
					contentDescription = null,
					modifier = Modifier.size(size = 24.dp)
				)
			}
		}
	}
}

@Composable
fun ClickableTextWithHtmlLinks(
	htmlText: String,
	textStyle: TextStyle = MaterialTheme.typography.bodyMedium,
	linkStyle: SpanStyle = SpanStyle(
		color = Color.Blue,
		textDecoration = TextDecoration.Underline
	)
) {
	val pattern = """<a +href="(?<link>[^"]+)">(?<text>.+?)</a>""".toRegex()
	val matchResult = pattern.findAll(htmlText)
	val fullTextLength = htmlText.length
	var cursor = 0

	val annotatedString = buildAnnotatedString {
		for (match in matchResult) {
			val matchStart = match.range.first
			val matchEnd = match.range.last
			val text = match.groups["text"]?.value ?: ""
			val link = match.groups["link"]?.value ?: ""

			if (cursor < matchStart) {
				append(
					text = htmlText.substring(
						startIndex = cursor,
						endIndex = matchStart
					)
				)
			}

			pushStringAnnotation(tag = "link", annotation = link)

			withStyle(style = linkStyle) {
				append(text = text)
			}

			pop()

			cursor = matchEnd + 1
		}

		if (cursor < fullTextLength) {
			append(
				text = htmlText.substring(
					startIndex = cursor,
					endIndex = fullTextLength
				)
			)
		}
	}

	val uriHandler = LocalUriHandler.current

	ClickableText(
		text = annotatedString,
		onClick = { offset ->
			annotatedString
				.getStringAnnotations(tag = "link", start = offset, end = offset)
				.firstOrNull()
				?.let { link -> uriHandler.openUri(link.item) }
		},
		style = textStyle
	)
}
