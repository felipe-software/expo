import { render } from '@testing-library/react-native';
import { View } from 'react-native';

import { RangeSlider, VerticalSlider } from '..';

const mockNativeViewFn = jest.fn();

jest.mock('expo', () => ({
  requireNativeView: jest.fn((moduleName, viewName) => {
    if (moduleName !== 'ExpoUI') {
      throw new Error(`Unexpected native module requested: ${moduleName}`);
    }
    const { View } = require('react-native');
    const { createElement } = require('react');
    const MockView = (props: any) => {
      mockNativeViewFn(viewName, props);
      return createElement(View, props);
    };
    return MockView;
  }),
}));

beforeEach(() => {
  mockNativeViewFn.mockClear();
});

function getVerticalSliderProps() {
  return mockNativeViewFn.mock.calls.find(([viewName]) => viewName === 'VerticalSliderView')?.[1];
}

function getRangeSliderProps() {
  return mockNativeViewFn.mock.calls.find(([viewName]) => viewName === 'RangeSliderView')?.[1];
}

describe('VerticalSlider', () => {
  it('passes slider props and reverseDirection to the native view', () => {
    render(
      <VerticalSlider value={0.4} min={-1} max={2} steps={5} enabled={false} reverseDirection />
    );

    expect(getVerticalSliderProps()).toEqual(
      expect.objectContaining({
        value: 0.4,
        min: -1,
        max: 2,
        steps: 5,
        enabled: false,
        reverseDirection: true,
      })
    );
  });

  it('unwraps native value change events', () => {
    const onValueChange = jest.fn();
    const onValueChangeFinished = jest.fn();
    render(
      <VerticalSlider onValueChange={onValueChange} onValueChangeFinished={onValueChangeFinished} />
    );

    const props = getVerticalSliderProps();
    props.onValueChange({ nativeEvent: { value: 0.75 } });
    props.onValueChangeFinished();

    expect(onValueChange).toHaveBeenCalledWith(0.75);
    expect(onValueChangeFinished).toHaveBeenCalledTimes(1);
  });

  it('renders custom thumb and track slots', () => {
    const { getByTestId } = render(
      <VerticalSlider>
        <VerticalSlider.Thumb>
          <View testID="vertical-slider-thumb" />
        </VerticalSlider.Thumb>
        <VerticalSlider.Track>
          <View testID="vertical-slider-track" />
        </VerticalSlider.Track>
      </VerticalSlider>
    );

    expect(getByTestId('vertical-slider-thumb')).toBeTruthy();
    expect(getByTestId('vertical-slider-track')).toBeTruthy();
  });
});

describe('RangeSlider', () => {
  it('passes range slider props to the native view', () => {
    const colors = { thumbColor: '#ff0000', activeTrackColor: '#00ff00' };
    render(
      <RangeSlider
        value={{ start: 2, end: 8 }}
        min={0}
        max={10}
        lowerLimit={1}
        upperLimit={9}
        steps={4}
        enabled={false}
        colors={colors}
      />
    );

    expect(getRangeSliderProps()).toEqual(
      expect.objectContaining({
        value: { start: 2, end: 8 },
        min: 0,
        max: 10,
        lowerLimit: 1,
        upperLimit: 9,
        steps: 4,
        enabled: false,
        colors,
      })
    );
  });

  it('uses defaults matching the full slider range', () => {
    render(<RangeSlider />);

    expect(getRangeSliderProps()).toEqual(
      expect.objectContaining({
        value: { start: 0, end: 1 },
        min: 0,
        max: 1,
        steps: 0,
        enabled: true,
      })
    );
  });

  it('unwraps native range value change events', () => {
    const onValueChange = jest.fn();
    const onValueChangeFinished = jest.fn();
    render(
      <RangeSlider onValueChange={onValueChange} onValueChangeFinished={onValueChangeFinished} />
    );

    const props = getRangeSliderProps();
    props.onValueChange({ nativeEvent: { start: 0.25, end: 0.75 } });
    props.onValueChangeFinished();

    expect(onValueChange).toHaveBeenCalledWith({ start: 0.25, end: 0.75 });
    expect(onValueChangeFinished).toHaveBeenCalledTimes(1);
  });

  it('renders custom thumb and track slots', () => {
    const { getByTestId } = render(
      <RangeSlider>
        <RangeSlider.Thumb>
          <View testID="range-slider-thumb" />
        </RangeSlider.Thumb>
        <RangeSlider.Track>
          <View testID="range-slider-track" />
        </RangeSlider.Track>
      </RangeSlider>
    );

    expect(getByTestId('range-slider-thumb')).toBeTruthy();
    expect(getByTestId('range-slider-track')).toBeTruthy();
  });
});
