package expo.modules.ui

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import expo.modules.kotlin.records.Field
import expo.modules.kotlin.records.Record
import expo.modules.kotlin.types.OptimizedRecord
import expo.modules.kotlin.viewevent.getValue
import expo.modules.kotlin.views.ComposeProps
import expo.modules.kotlin.views.FunctionalComposableScope
import expo.modules.kotlin.views.OptimizedComposeProps

@OptimizedRecord
data class RangeSliderValue(
  @Field val start: Float = 0.0f,
  @Field val end: Float = 1.0f
) : Record

@OptimizedComposeProps
data class RangeSliderProps(
  val value: RangeSliderValue = RangeSliderValue(),
  val min: Float = 0.0f,
  val max: Float = 1.0f,
  val lowerLimit: Float? = null,
  val upperLimit: Float? = null,
  val steps: Int = 0,
  val enabled: Boolean = true,
  val colors: SliderColors = SliderColors(),
  val modifiers: ModifierList = emptyList()
) : ComposeProps

private fun RangeSliderValue.coerceIn(lower: Float, upper: Float): RangeSliderValue {
  val coercedStart = start.coerceIn(lower, upper)
  val coercedEnd = end.coerceIn(lower, upper)
  return RangeSliderValue(
    start = minOf(coercedStart, coercedEnd),
    end = maxOf(coercedStart, coercedEnd)
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FunctionalComposableScope.RangeSliderContent(props: RangeSliderProps) {
  val onValueChange by remember { this@RangeSliderContent.EventDispatcher<RangeSliderValue>() }
  val onValueChangeFinished by remember { this@RangeSliderContent.EventDispatcher<Unit>() }
  val startInteractionSource = remember { MutableInteractionSource() }
  val endInteractionSource = remember { MutableInteractionSource() }

  val effectiveLower = maxOf(props.min, props.lowerLimit ?: Float.NEGATIVE_INFINITY)
  val effectiveUpper = minOf(props.max, props.upperLimit ?: Float.POSITIVE_INFINITY)
  val initialValue = props.value.coerceIn(effectiveLower, effectiveUpper)

  var localStart by remember { mutableFloatStateOf(initialValue.start) }
  var localEnd by remember { mutableFloatStateOf(initialValue.end) }
  var isDragging by remember { mutableStateOf(false) }
  var previousPropsStart by remember { mutableFloatStateOf(initialValue.start) }
  var previousPropsEnd by remember { mutableFloatStateOf(initialValue.end) }

  val coercedPropsValue = props.value.coerceIn(effectiveLower, effectiveUpper)
  // Controlled props arrive through the JS bridge after native drag events. Keep rendering the
  // local range while dragging so an older prop value cannot pull either thumb backwards.
  if (
    coercedPropsValue.start != previousPropsStart ||
    coercedPropsValue.end != previousPropsEnd
  ) {
    previousPropsStart = coercedPropsValue.start
    previousPropsEnd = coercedPropsValue.end
    if (!isDragging) {
      localStart = coercedPropsValue.start
      localEnd = coercedPropsValue.end
    }
  }

  val thumbSlotView = findChildSlotView(view, "thumb")
  val trackSlotView = findChildSlotView(view, "track")

  val sliderColors = SliderDefaults.colors(
    thumbColor = props.colors.thumbColor.compose,
    activeTrackColor = props.colors.activeTrackColor.compose,
    inactiveTrackColor = props.colors.inactiveTrackColor.compose,
    activeTickColor = props.colors.activeTickColor.compose,
    inactiveTickColor = props.colors.inactiveTickColor.compose
  )

  RangeSlider(
    value = localStart..localEnd,
    valueRange = props.min..props.max,
    steps = props.steps,
    enabled = props.enabled,
    startInteractionSource = startInteractionSource,
    endInteractionSource = endInteractionSource,
    onValueChange = {
      val coercedValue = RangeSliderValue(it.start, it.endInclusive)
        .coerceIn(effectiveLower, effectiveUpper)
      isDragging = true
      localStart = coercedValue.start
      localEnd = coercedValue.end
      onValueChange(coercedValue)
    },
    onValueChangeFinished = {
      isDragging = false
      onValueChangeFinished(Unit)
    },
    colors = sliderColors,
    startThumb = {
      if (thumbSlotView != null) {
        thumbSlotView.renderSlot()
      } else {
        SliderDefaults.Thumb(
          interactionSource = startInteractionSource,
          colors = sliderColors,
          enabled = props.enabled
        )
      }
    },
    endThumb = {
      if (thumbSlotView != null) {
        thumbSlotView.renderSlot()
      } else {
        SliderDefaults.Thumb(
          interactionSource = endInteractionSource,
          colors = sliderColors,
          enabled = props.enabled
        )
      }
    },
    track = { rangeSliderState ->
      if (trackSlotView != null) {
        trackSlotView.renderSlot()
      } else {
        SliderDefaults.Track(
          rangeSliderState = rangeSliderState,
          colors = sliderColors,
          enabled = props.enabled
        )
      }
    },
    modifier = ModifierRegistry.applyModifiers(
      props.modifiers,
      appContext,
      composableScope,
      globalEventDispatcher
    )
  )
}
